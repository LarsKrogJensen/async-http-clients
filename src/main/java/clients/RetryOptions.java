package clients;

public class RetryOptions {
    public final int attempts;
    public final long delayMs;

    public RetryOptions(int attempts, long delayMs) {
        if (attempts < 1) {
            throw new IllegalArgumentException("Attempts must me >= 1");
        }
        this.attempts = attempts;
        this.delayMs = delayMs;
    }
}
