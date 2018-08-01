package love.wangqi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:45
 */
public class ScriptUtil {
    public static String getScript(String path) {
        StringBuilder sb = new StringBuilder();

        InputStream stream = ScriptUtil.class.getClassLoader().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        try {

            String str = "";
            while ((str = br.readLine()) != null) {
                sb.append(str).append(System.lineSeparator());
            }

        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }
        return sb.toString();
    }
}
