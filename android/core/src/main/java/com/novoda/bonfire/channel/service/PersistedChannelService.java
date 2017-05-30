package com.novoda.bonfire.channel.service;

import com.novoda.bonfire.channel.data.model.Channel;
import com.novoda.bonfire.channel.data.model.Channels;
import com.novoda.bonfire.channel.database.ChannelsDatabase;
import com.novoda.bonfire.database.DatabaseResult;
import com.novoda.bonfire.user.data.model.User;
import com.novoda.bonfire.user.data.model.Users;
import com.novoda.bonfire.user.database.UserDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class PersistedChannelService implements ChannelService {

    private final ChannelsDatabase channelsDatabase;
    private final UserDatabase userDatabase;

    public PersistedChannelService(ChannelsDatabase channelsDatabase, UserDatabase userDatabase) {
        this.channelsDatabase = channelsDatabase;
        this.userDatabase = userDatabase;
    }

    @Override
    public Observable<Channels> getChannelsFor(User user) {
        return Observable.combineLatest(publicChannels(), privateChannelsFor(user), mergeChannels());
    }

    private Observable<List<Channel>> publicChannels() {
        return channelsDatabase.observePublicChannelIds()
                .flatMap(channelsFromNames());
    }

    private Observable<List<Channel>> privateChannelsFor(User user) {
        return channelsDatabase.observePrivateChannelIdsFor(user)
                .flatMap(channelsFromNames());
    }

    private Function<List<String>, Observable<List<Channel>>> channelsFromNames() {
        return new Function<List<String>, Observable<List<Channel>>>() {
            @Override
            public Observable<List<Channel>> apply(@NonNull List<String> channelNames) throws Exception {
                return Observable.fromIterable(channelNames)
                        .flatMap(channelFromName())
                        .toList()
                        .toObservable();
            }
        };
    }

    private Function<String, Observable<Channel>> channelFromName() {
        return new Function<String, Observable<Channel>>() {
            @Override
            public Observable<Channel> apply(@NonNull final String channelName) throws Exception {
                return channelsDatabase.readChannelFor(channelName);
            }
        };
    }

    private BiFunction<List<Channel>, List<Channel>, Channels> mergeChannels() {
        return new BiFunction<List<Channel>, List<Channel>, Channels>() {
            @Override
            public Channels apply(@NonNull List<Channel> channels, @NonNull List<Channel> channels2) throws Exception {
                List<Channel> mergedChannels = new ArrayList<>(channels);
                mergedChannels.addAll(channels2);
                return new Channels(mergedChannels);
            }
        };
    }

    @Override
    public Observable<DatabaseResult<Channel>> createPublicChannel(Channel newChannel) {
        return channelsDatabase.writeChannel(newChannel)
                .flatMap(writeChannelToChannelIndexDb())
                .onErrorReturn(DatabaseResult.<Channel>errorAsDatabaseResult());
    }

    private Function<Channel, Observable<DatabaseResult<Channel>>> writeChannelToChannelIndexDb() {
        return new Function<Channel, Observable<DatabaseResult<Channel>>>() {
            @Override
            public Observable<DatabaseResult<Channel>> apply(@NonNull Channel channel) throws Exception {
                return channelsDatabase.writeChannelToPublicChannelIndex(channel)
                        .map(new Function<Channel, DatabaseResult<Channel>>() {
                            @Override
                            public DatabaseResult<Channel> apply(@NonNull Channel channel) throws Exception {
                                return new DatabaseResult<>(channel);
                            }
                        });
            }
        };
    }

    @Override
    public Observable<DatabaseResult<Channel>> createPrivateChannel(final Channel newChannel, User owner) {
        return channelsDatabase.addOwnerToPrivateChannel(owner, newChannel)
                .flatMap(addUserAsChannelOwner(owner))
                .flatMap(writeChannel())
                .onErrorReturn(DatabaseResult.<Channel>errorAsDatabaseResult());
    }

    private Function<Channel, Observable<DatabaseResult<Channel>>> writeChannel() {
        return new Function<Channel, Observable<DatabaseResult<Channel>>>() {
            @Override
            public Observable<DatabaseResult<Channel>> apply(@NonNull Channel result) throws Exception {
                return channelsDatabase.writeChannel(result)
                        .map(new Function<Channel, DatabaseResult<Channel>>() {
                            @Override
                            public DatabaseResult<Channel> apply(@NonNull Channel channel) throws Exception {
                                return new DatabaseResult<>(channel);
                            }
                        });
            }
        };
    }

    @Override
    public Observable<DatabaseResult<User>> addOwnerToPrivateChannel(final Channel channel, final User newOwner) {
        return channelsDatabase.addOwnerToPrivateChannel(newOwner, channel)
                .flatMap(addUserAsChannelOwner(newOwner))
                .map(new Function<Channel, DatabaseResult<User>>() {
                    @Override
                    public DatabaseResult<User> apply(@NonNull Channel channel) throws Exception {
                        return new DatabaseResult<>(newOwner); //TODO maybe not the best ?
                    }
                })
                .onErrorReturn(DatabaseResult.<User>errorAsDatabaseResult());
    }

    private Function<Channel, Observable<Channel>> addUserAsChannelOwner(final User user) {
        return new Function<Channel, Observable<Channel>>() {
            @Override
            public Observable<Channel> apply(@NonNull final Channel channel) throws Exception {
                return channelsDatabase.addChannelToUserPrivateChannelIndex(user, channel);
            }
        };
    }

    @Override
    public Observable<DatabaseResult<User>> removeOwnerFromPrivateChannel(final Channel channel, final User removedOwner) {
        return channelsDatabase.removeOwnerFromPrivateChannel(removedOwner, channel)
                .flatMap(removeChannelReferenceFromUser(removedOwner))
                .map(new Function<Channel, DatabaseResult<User>>() {
                    @Override
                    public DatabaseResult<User> apply(@NonNull Channel channel) throws Exception {
                        return new DatabaseResult<>(removedOwner); //TODO maybe not the best ?
                    }
                })
                .onErrorReturn(DatabaseResult.<User>errorAsDatabaseResult());
    }

    private Function<Channel, Observable<Channel>> removeChannelReferenceFromUser(final User user) {
        return new Function<Channel, Observable<Channel>>() {
            @Override
            public Observable<Channel> apply(@NonNull Channel channel) throws Exception {
                return channelsDatabase.removeChannelFromUserPrivateChannelIndex(user, channel);
            }
        };
    }

    @Override
    public Observable<DatabaseResult<Users>> getOwnersOfChannel(Channel channel) {
        return channelsDatabase.observeOwnerIdsFor(channel)
                .flatMap(getUsersFromIds())
                .onErrorReturn(DatabaseResult.<Users>errorAsDatabaseResult());
    }

    private Function<List<String>, Observable<DatabaseResult<Users>>> getUsersFromIds() {
        return new Function<List<String>, Observable<DatabaseResult<Users>>>() {
            @Override
            public Observable<DatabaseResult<Users>> apply(@NonNull List<String> userIds) throws Exception {
                return Observable.fromIterable(userIds)
                        .flatMap(getUserFromId())
                        .toList()
                        .map(new Function<List<User>, DatabaseResult<Users>>() {
                            @Override
                            public DatabaseResult<Users> apply(@NonNull List<User> users) throws Exception {
                                return new DatabaseResult<>(new Users(users));
                            }
                        })
                        .toObservable();
            }
        };
    }

    private Function<String, Observable<User>> getUserFromId() {
        return new Function<String, Observable<User>>() {
            @Override
            public Observable<User> apply(@NonNull final String userId) throws Exception {
                return userDatabase.readUserFrom(userId);
            }
        };
    }
}
