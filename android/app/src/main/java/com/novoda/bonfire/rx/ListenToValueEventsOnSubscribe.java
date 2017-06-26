package com.novoda.bonfire.rx;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;

class ListenToValueEventsOnSubscribe<T> implements ObservableOnSubscribe<T> {

    private final Query query;
    private final Function<DataSnapshot, T> marshaller;

    ListenToValueEventsOnSubscribe(Query query, Function<DataSnapshot, T> marshaller) {
        this.query = query;
        this.marshaller = marshaller;
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception {
        final ValueEventListener eventListener = query.addValueEventListener(new RxValueListener<>(emitter, marshaller));
        emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                query.removeEventListener(eventListener);
            }
        });
    }

    private static class RxValueListener<T> implements ValueEventListener {

        private final ObservableEmitter<? super T> emitter;
        private final Function<DataSnapshot, T> marshaller;

        RxValueListener(ObservableEmitter<T> emitter, Function<DataSnapshot, T> marshaller) {
            this.emitter = emitter;
            this.marshaller = marshaller;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!emitter.isDisposed()) {
                try {
                    emitter.onNext(marshaller.apply(dataSnapshot));
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            emitter.onError(databaseError.toException()); //TODO handle errors in pipeline
        }

    }

}
