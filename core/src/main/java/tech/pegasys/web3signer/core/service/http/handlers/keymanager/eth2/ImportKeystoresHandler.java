package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImportKeystoresHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LogManager.getLogger();
    public static final int BAD_REQUEST = 400;

    private final ObjectMapper objectMapper;

    public ImportKeystoresHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(RoutingContext context) {
        final RequestParameters params = context.get("parsedParameters");
        final ImportKeystoresRequestBody parsedBody;
        try {
            parsedBody = parseRequestBody(params);
        } catch (final IllegalArgumentException | JsonProcessingException e) {
            handleInvalidRequest(context, e);
            return;
        }

        // TODO decode parsedBody.keystores
        // TODO import parsedBody.slashingProtection

        // TODO respond with success
        // {[{status: imported/duplicate/error, message: ""}]}
    }

    private ImportKeystoresRequestBody parseRequestBody(final RequestParameters params)
            throws JsonProcessingException {
        final String body = params.body().toString();
        return objectMapper.readValue(body, ImportKeystoresRequestBody.class);
    }

    private void handleInvalidRequest(final RoutingContext routingContext, final Exception e) {
        LOG.debug("Invalid import keystores request - " + routingContext.getBodyAsString(), e);
        routingContext.fail(BAD_REQUEST);
    }

    class ImportKeystoresRequestBody {
        private final String[] keystores;
        private final String[] passwords;
        private final String slashingProtection;

        @JsonCreator
        ImportKeystoresRequestBody(
                @JsonProperty("keystores") final String[] keystores,
                @JsonProperty("passwords") final String[] passwords,
                @JsonProperty("slashing_protection") final String slashingProtection) {
            this.keystores = keystores;
            this.passwords = passwords;
            this.slashingProtection = slashingProtection;
        }

        @JsonProperty("keystores")
        public String[] getKeystores() {
            return keystores;
        }

        @JsonProperty("passwords")
        public String[] getPasswords() {
            return passwords;
        }

        @JsonProperty("slashing_protection")
        public String getSlashingProtection() {
            return slashingProtection;
        }
    }
}
