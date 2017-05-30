package com.novoda.bonfire.helpers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.novoda.bonfire.rx.FirebaseObservableListeners;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class FirebaseTestHelpers {
    public static void setupDatabaseStubsFor(String databaseName, DatabaseReference databaseReference, FirebaseDatabase firebaseDatabase) {
        when(firebaseDatabase.getReference(databaseName)).thenReturn(databaseReference);
        when(databaseReference.child(anyString())).thenReturn(databaseReference);
        when(databaseReference.push()).thenReturn(databaseReference);
        when(databaseReference.limitToLast(anyInt())).thenReturn(databaseReference);
    }

    public static <T> void setupValueEventListenerFor(
            FirebaseObservableListeners listeners,
            DatabaseReference databaseReference,
            T returnValue) {
        setupValueEventListenerWithObservable(listeners, databaseReference, Observable.just(returnValue));
    }

    public static void setupErroringValueEventListenerFor(
            FirebaseObservableListeners listeners,
            DatabaseReference databaseReference,
            Throwable throwable) {
        setupValueEventListenerWithObservable(listeners, databaseReference, Observable.error(throwable));
    }

    private static <T> void setupValueEventListenerWithObservable(
            FirebaseObservableListeners listeners,
            DatabaseReference databaseReference,
            Observable<T> observable) {
        when(listeners.listenToValueEvents(
                eq(databaseReference),
                any(FirebaseTestHelpers.<T>marshallerType())
             )
        ).thenReturn(observable);
    }

    public static <T> void setupSingleValueEventListenerFor(
            FirebaseObservableListeners listeners,
            DatabaseReference databaseReference,
            T returnValue) {
        when(listeners.listenToSingleValueEvents(
                eq(databaseReference),
                any(FirebaseTestHelpers.<T>marshallerType())
             )
        ).thenReturn(Observable.just(returnValue));
    }

    public static <T> void assertValueReceivedOnNext(Observable<T> observable, T expectedValue) {
        TestObserver<T> testObserver = testObserverFor(observable);
        testObserver.assertValue(expectedValue);
    }

    public static <T> void assertThrowableReceivedOnError(Observable<T> observable, Throwable throwable) {
        TestObserver<T> testObserver = testObserverFor(observable);
        testObserver.assertError(throwable);
    }

    @NonNull
    private static <T> TestObserver<T> testObserverFor(Observable<T> observable) {
        TestObserver<T> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);
        return testObserver;
    }

    private static <T> Class<Function<DataSnapshot, T>> marshallerType() {
        return (Class<Function<DataSnapshot, T>>) (Class) Function.class;
    }
}
