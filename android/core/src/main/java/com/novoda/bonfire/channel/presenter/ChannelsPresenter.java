package com.novoda.bonfire.channel.presenter;

import com.novoda.bonfire.Config;
import com.novoda.bonfire.analytics.Analytics;
import com.novoda.bonfire.channel.data.model.Channel;
import com.novoda.bonfire.channel.data.model.Channels;
import com.novoda.bonfire.channel.displayer.ChannelsDisplayer;
import com.novoda.bonfire.channel.service.ChannelService;
import com.novoda.bonfire.link.LinkFactory;
import com.novoda.bonfire.login.data.model.Authentication;
import com.novoda.bonfire.login.service.LoginService;
import com.novoda.bonfire.navigation.Navigator;
import com.novoda.bonfire.user.data.model.User;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class ChannelsPresenter {

    private final ChannelsDisplayer channelsDisplayer;
    private final ChannelService channelService;
    private final LoginService loginService;
    private final Config config;
    private final Navigator navigator;
    private final LinkFactory linkFactory;
    private final Analytics analytics;

    private Disposable subscription;
    private User user;

    public ChannelsPresenter(
            ChannelsDisplayer channelsDisplayer,
            ChannelService channelService,
            LoginService loginService,
            Config config,
            Navigator navigator,
            LinkFactory linkFactory,
            Analytics analytics
    ) {
        this.channelsDisplayer = channelsDisplayer;
        this.channelService = channelService;
        this.loginService = loginService;
        this.config = config;
        this.navigator = navigator;
        this.linkFactory = linkFactory;
        this.analytics = analytics;
    }

    public void startPresenting() {
        channelsDisplayer.attach(channelsInteractionListener);
        subscription = loginService.getAuthentication()
                .filter(successfullyAuthenticated())
                .doOnNext(new Consumer<Authentication>() {
                    @Override
                    public void accept(@NonNull Authentication authentication) throws Exception {
                        user = authentication.getUser();
                    }
                })
                .flatMap(channelsForUser())
                .map(sortIfConfigured())
                .subscribe(new Consumer<Channels>() {
                    @Override
                    public void accept(@NonNull Channels channels) throws Exception {
                        channelsDisplayer.display(channels);
                    }
                });
    }

    private Function<Authentication, Observable<Channels>> channelsForUser() {
        return new Function<Authentication, Observable<Channels>>() {
            @Override
            public Observable<Channels> apply(@NonNull Authentication authentication) throws Exception {
                return channelService.getChannelsFor(authentication.getUser());
            }
        };
    }

    private Function<Channels, Channels> sortIfConfigured() {
        return new Function<Channels, Channels>() {
            @Override
            public Channels apply(@NonNull Channels channels) throws Exception {
                if (config.orderChannelsByName()) {
                    return channels.sortedByName();
                } else {
                    return channels;
                }
            }
        };
    }

    private Predicate<? super Authentication> successfullyAuthenticated() {
        return new Predicate<Authentication>() {
            @Override
            public boolean test(@NonNull Authentication authentication) throws Exception {
                return authentication.isSuccess();
            }
        };
    }

    public void stopPresenting() {
        subscription.dispose();
        channelsDisplayer.detach(channelsInteractionListener);
    }

    private final ChannelsDisplayer.ChannelsInteractionListener channelsInteractionListener = new ChannelsDisplayer.ChannelsInteractionListener() {
        @Override
        public void onChannelSelected(Channel channel) {
            analytics.trackSelectChannel(channel.getName());
            navigator.toChannel(channel);
        }

        @Override
        public void onAddNewChannel() {
            analytics.trackCreateChannel(user.getId());
            navigator.toCreateChannel();
        }

        @Override
        public void onInviteUsersClicked() {
            analytics.trackSendInvitesSelected(user.getId());
            navigator.toShareInvite(linkFactory.inviteLinkFrom(user).toString());
        }
    };
}
