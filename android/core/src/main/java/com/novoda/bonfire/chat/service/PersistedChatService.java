package com.novoda.bonfire.chat.service;

import com.novoda.bonfire.channel.data.model.Channel;
import com.novoda.bonfire.chat.data.model.Chat;
import com.novoda.bonfire.chat.data.model.Message;
import com.novoda.bonfire.chat.database.ChatDatabase;
import com.novoda.bonfire.database.DatabaseResult;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class PersistedChatService implements ChatService {

    private final ChatDatabase chatDatabase;

    public PersistedChatService(ChatDatabase chatDatabase) {
        this.chatDatabase = chatDatabase;
    }

    @Override
    public Observable<DatabaseResult<Chat>> getChat(final Channel channel) {
        return chatDatabase.observeChat(channel)
                .map(asDatabaseResult())
                .onErrorReturn(DatabaseResult.<Chat>errorAsDatabaseResult());
    }

    private static Function<Chat, DatabaseResult<Chat>> asDatabaseResult() {
        return new Function<Chat, DatabaseResult<Chat>>() {
            @Override
            public DatabaseResult<Chat> apply(@NonNull Chat chat) throws Exception {
                return new DatabaseResult<Chat>(chat);
            }
        };
    }

    @Override
    public void sendMessage(Channel channel, Message message) {
        chatDatabase.sendMessage(channel, message);
    }

}
