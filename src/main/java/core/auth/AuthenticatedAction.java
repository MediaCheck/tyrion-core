package core.auth;

import com.google.inject.Injector;
import core.http.Results;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class AuthenticatedAction extends Action<Authenticated> {

    private final Function<Authenticated, Authenticator> configurator;

    @Inject
    public AuthenticatedAction(Injector injector) {
        this.configurator = authenticated -> injector.getInstance(authenticated.value());
    }

    @Override
    public CompletionStage<Result> call(Http.Request req) {
        Authenticator authenticator = configurator.apply(configuration);
        return authenticator.authenticate(req)
                .map(authenticated -> this.delegate.call(authenticated))
                .orElseGet(() -> CompletableFuture.completedFuture(Results.unauthorized()));
    }
}
