package com.xin.util;

import com.xin.ZkConfService.ZkConf;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class CommandUtils {
    public static String result(String command, ZkConf zkConf) throws IOException {
        String[] split = zkConf.getAddress()
                               .split(":");
        Socket socket = new Socket(split[0], Integer.parseInt(split[1]));
        socket.getOutputStream()
              .write(command.getBytes(StandardCharsets.UTF_8));
        String s = IOUtils.toString(socket.getInputStream(), StandardCharsets.UTF_8);
        socket.close();
        return s;
    }
}
