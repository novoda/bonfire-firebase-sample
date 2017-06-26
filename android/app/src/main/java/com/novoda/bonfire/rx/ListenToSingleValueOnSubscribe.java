package com.novoda.bonfire.rx;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

class ListenToSingleValueOnSubscribe<T> implements ObservableOnSubscribe<T> {

    private final Query query;
    private final Function<DataSnapshot, T> marshaller;

    ListenToSingleValueOnSubscribe(Query query, Function<DataSnapshot, T> marshaller) {
        this.query = query;
        this.marshaller = marshaller;
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception {
        query.addListenerForSingleValueEvent(new RxSingleValueListener<>(emitter, marshaller));
    }

    private static class RxSingleValueListener<T> implements ValueEventListener {

        private final ObservableEmitter<? super T> emitter;
        private final Function<DataSnapshot, T> marshaller;

        public RxSingleValueListener(ObservableEmitter<T> emitter, Function<DataSnapshot, T> marshaller) {
            this.emitter = emitter;
            this.marshaller = marshaller;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren() && !emitter.isDisposed()) {
                try {
                    emitter.onNext(marshaller.apply(dataSnapshot));
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
            emitter.onComplete();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            emitter.onError(databaseError.toException()); //TODO handle errors in pipeline
        }

    }
}
