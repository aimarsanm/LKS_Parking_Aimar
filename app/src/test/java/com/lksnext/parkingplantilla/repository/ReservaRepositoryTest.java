package com.lksnext.parkingplantilla.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.Usuario;
import com.lksnext.parkingplantilla.viewmodel.ReservaViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReservaRepositoryTest {

    private ReservaRepository reservaRepository;

    // Mocks para todas las dependencias de Firebase
    @Mock private FirebaseFirestore db;
    @Mock private FirebaseAuth auth;
    @Mock private FirebaseUser firebaseUser;
    @Mock private CollectionReference collectionReference;
    @Mock private DocumentReference documentReference;
    @Mock private Query query;
    @Mock private Task<QuerySnapshot> taskQuerySnapshot;
    @Mock private Task<DocumentReference> taskDocumentReference;
    @Mock private Task<Void> taskVoid;
    @Mock private QuerySnapshot querySnapshot;
    @Mock private DocumentSnapshot documentSnapshot;

    // Mocks para los callbacks de la aplicación
    @Mock private ReservaRepository.FirestoreCallback firestoreCallback;
    @Mock private ReservaRepository.ReservaListCallback reservaListCallback;
    @Mock private ReservaRepository.UsuarioCallback usuarioCallback;

    // Captors para verificar los datos pasados a los callbacks
    @Captor private ArgumentCaptor<Reserva> reservaCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;
    @Captor private ArgumentCaptor<List<Reserva>> reservaListCaptor;
    @Captor private ArgumentCaptor<OnSuccessListener<QuerySnapshot>> querySuccessListenerCaptor;
    @Captor private ArgumentCaptor<OnSuccessListener<DocumentReference>> documentReferenceSuccessListenerCaptor;
    @Captor private ArgumentCaptor<OnCompleteListener<QuerySnapshot>> completeListenerCaptor;
    @Captor private ArgumentCaptor<OnSuccessListener<Void>> voidSuccessListenerCaptor;
    @Captor private ArgumentCaptor<OnFailureListener> failureListenerCaptor;
    @Captor private ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> documentSnapshotSuccessListenerCaptor;


    // Clases estáticas que necesitan ser mockeadas
    private MockedStatic<FirebaseFirestore> mockedFirestore;
    private MockedStatic<FirebaseAuth> mockedAuth;
    private MockedStatic<Log> mockedLog;
    private MockedStatic<DataRepository> mockedDataRepository;

    @Before
    public void setUp() {
        // 1. Mockear las clases estáticas (Firebase, Log de Android, DataRepository)
        mockedFirestore = Mockito.mockStatic(FirebaseFirestore.class);
        mockedAuth = Mockito.mockStatic(FirebaseAuth.class);
        mockedLog = Mockito.mockStatic(Log.class);
        mockedDataRepository = Mockito.mockStatic(DataRepository.class);

        // 2. Definir el comportamiento de los mocks estáticos
        mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(db);
        mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
        DataRepository dataRepositoryMock = mock(DataRepository.class);
        mockedDataRepository.when(DataRepository::getInstance).thenReturn(dataRepositoryMock);

        // 3. Instanciar el repositorio DESPUÉS de mockear las clases estáticas
        reservaRepository = new ReservaRepository();

        // 4. Configurar el comportamiento general de los mocks de Firebase
        when(auth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("testUid");
        when(dataRepositoryMock.getCurrentUser()).thenReturn(firebaseUser);

        // Navegación por la estructura de Firestore
        when(db.collection(anyString())).thenReturn(collectionReference);
        when(db.collectionGroup(anyString())).thenReturn(query);
        when(collectionReference.document(anyString())).thenReturn(documentReference);
        when(documentReference.collection(anyString())).thenReturn(collectionReference);

        // Consultas y operaciones
        when(collectionReference.whereEqualTo(anyString(), any())).thenReturn(query);
        when(query.whereEqualTo(anyString(), any())).thenReturn(query); // Para whereEqualTo encadenados
        when(query.get()).thenReturn(taskQuerySnapshot);
        when(collectionReference.get()).thenReturn(taskQuerySnapshot); // Para get() directos sobre una colección
        when(collectionReference.add(any(Reserva.class))).thenReturn(taskDocumentReference);
        when(documentReference.set(any(Reserva.class))).thenReturn(taskVoid);
        when(documentReference.delete()).thenReturn(taskVoid);
        when(documentReference.get()).thenReturn(mock(Task.class));
    }

    @After
    public void tearDown() {
        // Liberar los mocks estáticos para no afectar a otras pruebas
        mockedFirestore.close();
        mockedAuth.close();
        mockedLog.close();
        mockedDataRepository.close();
    }

    @Test
    public void guardarReservaFirestore_Exito() {
        // --- Arrange (Preparación) ---
        // Simula que no hay ninguna reserva previa para la fecha y tipo de plaza.
        // ENTRADA: Firestore devolverá una lista vacía.
        when(querySnapshot.iterator()).thenReturn(Collections.emptyIterator());

        // Configura las tareas para que capturen sus listeners de éxito.
        when(taskQuerySnapshot.addOnSuccessListener(querySuccessListenerCaptor.capture())).thenReturn(taskQuerySnapshot);
        when(taskDocumentReference.addOnSuccessListener(documentReferenceSuccessListenerCaptor.capture())).thenReturn(taskDocumentReference);

        // --- Act (Ejecución) ---
        Reserva nuevaReserva = new Reserva("2025-07-10", "testUid", "id", new Plaza(0, "Coche"), new Hora(9, 0));
        reservaRepository.guardarReservaFirestore(nuevaReserva, firestoreCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la primera consulta (búsqueda de plazas) tiene éxito.
        querySuccessListenerCaptor.getValue().onSuccess(querySnapshot);

        // 2. Simula que la segunda operación (añadir la reserva) también tiene éxito.
        documentReferenceSuccessListenerCaptor.getValue().onSuccess(documentReference);

        // SALIDA: Se debe llamar al método `add` en la colección de reservas.
        verify(collectionReference).add(reservaCaptor.capture());
        // SALIDA: La reserva guardada debe tener la primera plaza libre (ID 1).
        assertEquals(1, reservaCaptor.getValue().getPlazaId().getId());
        // SALIDA: Se debe invocar el callback `onSuccess` final.
        verify(firestoreCallback).onSuccess();
        verify(firestoreCallback, never()).onFailure(any());
    }
    /*
        @Test
    public void guardarReservaFirestore_SinPlazas_Falla() {
        // --- Arrange (Preparación) ---
        // Simula que todas las plazas de coche (50) ya están reservadas a esa hora.
        // ENTRADA: Firestore devuelve una lista con 50 reservas.
        List<QueryDocumentSnapshot> docs = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Reserva r = new Reserva("2025-07-10", "uid", "id"+i, new Plaza(i, "Coche"), new Hora(9,0));
            r.setHoraFin(new Hora(10,0));
            QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
            when(doc.toObject(Reserva.class)).thenReturn(r);
            docs.add(doc);
        }
        when(querySnapshot.iterator()).thenReturn(docs.iterator());

        // Configura la tarea para que capture su listener de éxito.
        when(taskQuerySnapshot.addOnSuccessListener(querySuccessListenerCaptor.capture())).thenReturn(taskQuerySnapshot);

        // --- Act (Ejecución) ---
        Reserva nuevaReserva = new Reserva("2025-07-10", "testUid", "id", new Plaza(0, "Coche"), new Hora(9, 0));
        reservaRepository.guardarReservaFirestore(nuevaReserva, firestoreCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la consulta (búsqueda de plazas) tiene éxito.
        if (querySuccessListenerCaptor.getAllValues().size() > 0 && querySuccessListenerCaptor.getValue() != null) {
            querySuccessListenerCaptor.getValue().onSuccess(querySnapshot);
        }

        // SALIDA: Se debe invocar el callback `onFailure` con el mensaje correcto.
        verify(firestoreCallback).onFailure(exceptionCaptor.capture());
        assertEquals("No hay plazas libres disponibles", exceptionCaptor.getValue().getMessage());
        // SALIDA: No se debe intentar añadir la reserva.
        verify(collectionReference, never()).add(any());
    }*/
    @Test
    public void getReservasPorUsuarioFirestore_Exito() {
        // --- Arrange (Preparación) ---
        // Simula que Firestore encuentra una reserva para el usuario.
        // ENTRADA: La tarea de Firestore se completará con éxito y devolverá un QuerySnapshot con un documento.
        Reserva reservaMock = new Reserva("2025-07-10", "testUid", "reservaId1", new Plaza(1, "Coche"), new Hora(9,0));
        List<QueryDocumentSnapshot> docs = new ArrayList<>();
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        when(doc.toObject(Reserva.class)).thenReturn(reservaMock);
        when(doc.getId()).thenReturn("reservaId1");
        docs.add(doc);
        when(querySnapshot.iterator()).thenReturn(docs.iterator());

        // Configura la tarea para que capture el listener de finalización.
        when(taskQuerySnapshot.addOnCompleteListener(completeListenerCaptor.capture())).thenReturn(taskQuerySnapshot);

        // --- Act (Ejecución) ---
        reservaRepository.getReservasPorUsuarioFirestore("testUid", reservaListCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la tarea se completa con éxito.
        when(taskQuerySnapshot.isSuccessful()).thenReturn(true);
        when(taskQuerySnapshot.getResult()).thenReturn(querySnapshot);
        completeListenerCaptor.getValue().onComplete(taskQuerySnapshot);

        // SALIDA: Se debe invocar el callback `onReservasLoaded` con la lista de reservas.
        verify(reservaListCallback).onReservasLoaded(reservaListCaptor.capture());
        assertEquals(1, reservaListCaptor.getValue().size());
        assertEquals("reservaId1", reservaListCaptor.getValue().get(0).getId());
        verify(reservaListCallback, never()).onError(any());
    }

    @Test
    public void getReservasPorUsuarioFirestore_Fallo() {
        // --- Arrange (Preparación) ---
        // Simula que la consulta a Firestore falla.
        // ENTRADA: La tarea de Firestore se completará sin éxito y con una excepción.
        Exception exception = new Exception("Error de red simulado");

        // Configura la tarea para que capture el listener de finalización.
        when(taskQuerySnapshot.addOnCompleteListener(completeListenerCaptor.capture())).thenReturn(taskQuerySnapshot);

        // --- Act (Ejecución) ---
        reservaRepository.getReservasPorUsuarioFirestore("testUid", reservaListCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la tarea falla.
        when(taskQuerySnapshot.isSuccessful()).thenReturn(false);
        when(taskQuerySnapshot.getException()).thenReturn(exception);
        completeListenerCaptor.getValue().onComplete(taskQuerySnapshot);

        // SALIDA: Se debe invocar el callback `onError` con la excepción.
        verify(reservaListCallback).onError(exception);
        verify(reservaListCallback, never()).onReservasLoaded(any());
    }

    @Test
    public void cancelarReservaFirestore_Exito() {
        // --- Arrange (Preparación) ---
        // Simula que la operación de borrado en Firestore se completa con éxito.
        // ENTRADA: La tarea de borrado (delete) capturará a su listener `onSuccess`.
        when(taskVoid.addOnSuccessListener(voidSuccessListenerCaptor.capture())).thenReturn(taskVoid);

        // --- Act (Ejecución) ---
        reservaRepository.cancelarReservaFirestore("reservaId1", firestoreCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la tarea de borrado tiene éxito.
        voidSuccessListenerCaptor.getValue().onSuccess(null);

        // SALIDA: Se debe llamar al método `delete` en el `documentReference` correcto.
        verify(documentReference).delete();
        // SALIDA: Se debe invocar el callback `onSuccess`.
        verify(firestoreCallback).onSuccess();
        verify(firestoreCallback, never()).onFailure(any());
    }

    @Test
    public void editarReservaFirestore_Exito() {
        // --- Arrange (Preparación) ---
        // Simula que la operación de escritura (set) en Firestore se completa con éxito.
        // ENTRADA: La tarea de escritura (set) capturará a su listener `onSuccess`.
        when(taskVoid.addOnSuccessListener(voidSuccessListenerCaptor.capture())).thenReturn(taskVoid);

        // --- Act (Ejecución) ---
        Reserva nuevaReserva = new Reserva();
        reservaRepository.editarReservaFirestore("reservaId1", nuevaReserva, firestoreCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la tarea de escritura tiene éxito.
        voidSuccessListenerCaptor.getValue().onSuccess(null);

        // SALIDA: Se debe llamar al método `set` en el `documentReference` con la nueva reserva.
        verify(documentReference).set(nuevaReserva);
        // SALIDA: Se debe invocar el callback `onSuccess`.
        verify(firestoreCallback).onSuccess();
        verify(firestoreCallback, never()).onFailure(any());
    }

    @Test
    public void leerDatosUsuarioFirestore_Exito() {
        // --- Arrange (Preparación) ---
        // Simula que la lectura del documento del usuario se completa con éxito.
        // ENTRADA: La tarea `get` devuelve un `DocumentSnapshot` que al convertirse a objeto da nuestro `Usuario` mock.
        Task<DocumentSnapshot> taskDocumentSnapshot = mock(Task.class);
        when(documentReference.get()).thenReturn(taskDocumentSnapshot);
        Usuario usuarioMock = new Usuario("test@test.com", "Test User", "Coche", "1234ABC");
        when(documentSnapshot.toObject(Usuario.class)).thenReturn(usuarioMock);

        // Configura la tarea para que capture el listener de éxito.
        when(taskDocumentSnapshot.addOnSuccessListener(documentSnapshotSuccessListenerCaptor.capture())).thenReturn(taskDocumentSnapshot);

        // --- Act (Ejecución) ---
        reservaRepository.leerDatosUsuarioFirestore(usuarioCallback);

        // --- Assert (Verificación) ---
        // 1. Simula que la tarea de lectura tiene éxito.
        documentSnapshotSuccessListenerCaptor.getValue().onSuccess(documentSnapshot);

        // SALIDA: Se debe invocar el callback `onUsuarioLoaded` con el objeto Usuario.
        verify(usuarioCallback).onUsuarioLoaded(usuarioMock);
    }
}
