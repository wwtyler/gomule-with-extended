package gomule.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * MDC-style nested context for {@link D2Log}. Call {@link #push(String, Object)}
 * inside a try-with-resources to stamp every log line emitted on the current
 * thread with the provided key/value until the block exits.
 *
 * <p>Typical usage:
 * <pre>
 * try (D2LogContext c = D2LogContext.push("file", fileName)
 *                                   .push("item", "#" + i + "@0x" + Integer.toHexString(off))) {
 *     ... // any D2Log.* call inside here gets [file=... item=...] appended
 * }
 * </pre>
 *
 * <p>Insertion order is preserved so the rendered tag follows the push order.
 */
public final class D2LogContext implements AutoCloseable {

    private static final ThreadLocal<Deque<Map.Entry<String, String>>> STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private final int frames;

    private D2LogContext(int frames) {
        this.frames = frames;
    }

    /** Push a single key/value frame; close() pops it. */
    public static D2LogContext push(String key, Object value) {
        STACK.get().push(Map.entry(key, String.valueOf(value)));
        return new D2LogContext(1);
    }

    /** Chain another frame onto this context; close() pops all of them at once. */
    public D2LogContext and(String key, Object value) {
        STACK.get().push(Map.entry(key, String.valueOf(value)));
        return new D2LogContext(this.frames + 1);
    }

    @Override
    public void close() {
        Deque<Map.Entry<String, String>> stack = STACK.get();
        for (int i = 0; i < frames && !stack.isEmpty(); i++) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            STACK.remove();
        }
    }

    /** Render current context as " [k1=v1 k2=v2 ...]" or "" when empty. */
    static String render() {
        Deque<Map.Entry<String, String>> stack = STACK.get();
        if (stack.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(" [");
        // Iterate oldest-to-newest so the rendered chain reflects push() order.
        Object[] arr = stack.toArray();
        for (int i = arr.length - 1; i >= 0; i--) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> e = (Map.Entry<String, String>) arr[i];
            if (i != arr.length - 1) sb.append(' ');
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.append(']').toString();
    }
}
