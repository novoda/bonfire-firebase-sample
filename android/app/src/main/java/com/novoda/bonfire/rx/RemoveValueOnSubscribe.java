package com.novoda.bonfire.rx;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

class RemoveValueOnSubscribe<T> implements ObservableOnSubscribe<T> {

    private final DatabaseReference databaseReference;
    private final T returnValue;

    RemoveValueOnSubscribe(DatabaseReference databaseReference, T returnValue) {
        this.databaseReference = databaseReference;
        this.returnValue = returnValue;
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception {
        databaseReference.removeValue(new RxCompletionListener<>(emitter, returnValue));
    }

    private static class RxCompletionListener<T> implements DatabaseReference.CompletionListener {

        private final ObservableEmitter<? super T> emitter;
        private final T successValue;

        RxCompletionListener(@NonNull ObservableEmitter<T> emitter, T successValue) {
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
