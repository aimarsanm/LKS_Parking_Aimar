package com.lksnext.parkingplantilla.data;
//abcdefghi
import com.lksnext.parkingplantilla.domain.Callback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingplantilla.domain.Usuario;

public class DataRepository {

    public interface UserCallback {
        void onSuccess(Usuario usuario);
        void onFailure(Exception e);
    }

    public interface ExistsCallback {
        void onResult(boolean exists, Usuario usuario);
        void onFailure(Exception e);
    }

    private static DataRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private DataRepository(){
        mAuth = FirebaseAuth.getInstance();
    }

    //Creación de la instancia en caso de que no exista.
    public static synchronized DataRepository getInstance(){
        if (instance==null){
            instance = new DataRepository();
        }
        return instance;
    }

    // Registro de usuario en Auth y Firestore
    public void registerWithUserData(String email, String pass, String nombre, String apellido, Callback callback){
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Guardar datos extra en Firestore
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference ref = db.collection("users").document(user.getUid());
                        Usuario usuario = new Usuario(nombre, apellido, email, null);
                        ref.set(usuario);
                    }
                    callback.onSuccess();
                } else {
                    callback.onFailure("Error al registrar usuario");
                }
            });
    }

    // Comprobar si existe usuario en Firestore
    public void existeUsuario(String email, ExistsCallback callback) {
        usersRef.document(email)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                    callback.onResult(true, usuario);
                } else {
                    callback.onResult(false, null);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    // Guardar usuario en Firestore (sin registro Auth)
    public void guardarUsuarioFirestore(Usuario usuario, UserCallback callback) {
        usersRef.document(usuario.getEmail())
            .set(usuario)
            .addOnSuccessListener(aVoid -> callback.onSuccess(usuario))
            .addOnFailureListener(callback::onFailure);
    }

    // Login seguro con FirebaseAuth
    public void login(String email, String pass, Callback callback){
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                    callback.onFailure(errorMsg);
                }
            });
    }

    //Obtener usuario actual
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Leer datos de usuario desde Firestore
    public void leerDatosUsuarioFirestore(UsuarioCallback callback) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                callback.onUsuarioLoaded(usuario);
            })
            .addOnFailureListener(e -> callback.onUsuarioLoaded(null));
    }

    // Enviar email de recuperación de contraseña
    public void sendPasswordResetEmail(String email, Callback callback) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                    callback.onFailure(errorMsg);
                }
            });
    }

    public interface UsuarioCallback {
        void onUsuarioLoaded(Usuario usuario);
    }
}
