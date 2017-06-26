package com.novoda.bonfire.channel.presenter;

import com.novoda.bonfire.analytics.ErrorLogger;
import com.novoda.bonfire.channel.data.model.Channel;
import com.novoda.bonfire.channel.data.model.Channel.Access;
import com.novoda.bonfire.channel.displayer.NewChannelDisplayer;
import com.novoda.bonfire.channel.service.ChannelService;
import com.novoda.bonfire.database.DatabaseResult;
import com.novoda.bonfire.login.data.model.Authentication;
import com.novoda.bonfire.login.service.LoginService;
import com.novoda.bonfire.navigation.Navigator;
import com.novoda.bonfire.user.data.model.User;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class NewChannelPresenter {

    private final NewChannelDisplayer newChannelDisplayer;
    private final ChannelService channelService;
    private final LoginService loginService;
    private final Navigator navigator;
    private final ErrorLogger errorLogger;
    private User user;
    private CompositeDisposable subscriptions = new CompositeDisposable();

    public NewChannelPresenter(NewChannelDisplayer newChannelDisplayer,
                               ChannelService channelService,
                               LoginService loginService,
                               Navigator navigator,
                               ErrorLogger errorLogger) {
        this.newChannelDisplayer = newChannelDisplayer;
        this.channelService = channelService;
        this.loginService = loginService;
        this.navigator = navigator;
        this.errorLogger = errorLogger;
    }

    public void startPresenting() {
        newChannelDisplayer.attach(channelCreationListener);
        subscriptions.add(
                loginService.getAuthentication().subscribe(new Consumer<Authentication>() {
                    @Override
                    public void accept(@NonNull Authentication authentication) throws Exception {
                        user = authentication.getUser();
                    }
                })
        );
    }

    public void stopPresenting() {
        newChannelDisplayer.detach(channelCreationListener);
        subscriptions.clear();
        subscriptions = new CompositeDisposable();
    }

    private NewChannelDisplayer.ChannelCreationListener channelCreationListener = new NewChannelDisplayer.ChannelCreationListener() {

        @Override
        public void onCreateChannelClicked(String channelName, boolean isPrivate) {
            Channel newChannel = new Channel(channelName.trim(), isPrivate ? Access.PRIVATE : Access.PUBLIC);
            subscriptions.add(
                    create(newChannel).subscribe(new Consumer<DatabaseResult<Channel>>() {
                        @Override
                        public void accept(@NonNull DatabaseResult<Channel> databaseResult) throws Exception {
                            if (databaseResult.isSuccess()) {
                                navigator.toChannelWithClearedHistory(databaseResult.getData());
                            } else {
                                errorLogger.reportError(databaseResult.getFailure(), "Channel creation failed");
                                newChannelDisplayer.showChannelCreationError();
                            }
                        }
                    })
            );
        }

        @Override
        public void onCancel() {
            navigator.toParent();
        }
    };

    private Observable<DatabaseResult<Channel>> create(Channel newChannel) {
        if (newChannel.isPrivate()) {
            return channelService.createPrivateChannel(newChannel, user);
        } else {
            return channelService.createPublicChannel(newChannel);
        }
    }

}
