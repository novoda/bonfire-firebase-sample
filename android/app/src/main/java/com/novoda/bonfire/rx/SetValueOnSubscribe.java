package com.novoda.bonfire.rx;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

class SetValueOnSubscribe<T, U> implements ObservableOnSubscribe<U> {

    private final T value;
    private final DatabaseReference databaseReference;
    private final U returnValue;

    SetValueOnSubscribe(T value, DatabaseReference databaseReference, U returnValue) {
        this.value = value;
        this.databaseReference = databaseReference;
        this.returnValue = returnValue;
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<U> emitter) throws Exception {
        databaseReference.setValue(value, new RxCompletionListener<>(emitter, returnValue));
    }

    private static class RxCompletionListener<T> implements DatabaseReference.CompletionListener {

        private final ObservableEmitter<? super T> emitter;
        private final T successValue;

        RxCompletionListener(ObservableEmitter<? super T> emitter, T successValue) {
            this.emitter = emitter;
            this.successValue = successValue;
        }

        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError == null) {
                emitter.onNext(successValue);
                emitter.onComplete();
            } else {
                emitter.onError(databaseError.toException());
            }
        }

    }
}
