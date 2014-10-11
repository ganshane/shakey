package shakey.server.app;

import shakey.services.AppBootstrap;

/**
 * bootstrap
 *
 * @author jcai
 */
public class ShakeyServerBootstrap extends AppBootstrap {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("main class is null");
        } else {
            if (args.length > 1) {
                String[] appArgs = new String[args.length - 1];
                System.arraycopy(args, 1, appArgs, 0, appArgs.length);
                start(args[0], appArgs);
            } else {
                start(args[0], new String[]{});
            }
        }
    }
}
