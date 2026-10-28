package fieldservice;

import java.io.IOException;

public interface ErrorReporter {
    void capture(String exceptionPayload, String requestId) throws IOException, InterruptedException;
}
