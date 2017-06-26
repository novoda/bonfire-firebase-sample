package com.novoda.bonfire.rx;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ListenToValueEventsOnSubscribeTest {

    private static final String EXPECTED_KEY = "EXPECTED_KEY";

    @Test
    public void canGetKeyFromDataSnapshot() {

        final DataSnapshot expectedKeyData = mock(DataSnapshot.class);
        when(expectedKeyData.getKey()).thenReturn(EXPECTED_KEY);

        Query query = mock(Query.class);

        doAnswer(new Answer<ValueEventListener>() {
            @Override
            public ValueEventListener answer(InvocationOnMock invocation) throws Throwable {
                ValueEventListener valueEventListener = (ValueEventListener) invocation.getArguments()[0];
                valueEventListener.onDataChange(expectedKeyData);
                return valueEventListener;
            }
        }).when(query).addValueEventListener(any(ValueEventListener.class));

        Observable<String> observable = Observable.create(new ListenToValueEventsOnSubscribe<>(query, getKey()));

        TestObserver<String> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        testObserver.assertValues(EXPECTED_KEY);
    }

    private static Function<DataSnapshot, String> getKey() {
        return new Function<DataSnapshot, String>() {
            @Override
            public String apply(@NonNull DataSnapshot dataSnapshot) throws Exception {
                return dataSnapshot.getKey();
            }
        };
    }

}
