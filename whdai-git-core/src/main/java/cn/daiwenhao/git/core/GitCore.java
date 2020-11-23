package cn.daiwenhao.git.core;

import cn.daiwenhao.tools.Commander;
import cn.daiwenhao.tools.FlushOutputStream;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class GitCore {

    /**
     * 获取git仓库基础信息
     * @param repository git仓库路径
     * @param service 服务类型
     * @param response http response
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static void getRepoInfo(String repository, String service, HttpServletResponse response) throws IOException, InterruptedException {
        OutputStream out = response.getOutputStream();
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-" + service + "-advertisement");
        if (!getRepoInfo(repository, service, out)){
            response.setStatus(404);
        }
    }

    /**
     * 获取git仓库基础信息
     * @param repository git仓库路径
     * @param service 服务类型
     * @param out 输出流，返回给客户端的数据
     * @return 是否执行成功
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private static boolean getRepoInfo(String repository, String service, OutputStream out) throws IOException, InterruptedException {
        String cmd = String.format("git %s --stateless-rpc --advertise-refs %s", service.substring(4), repository);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        if (Commander.exec(cmd, null, result) == 0){
            //必要的返回信息
            byte[] info = String.format("%04x# service=%s0000", service.length()+14, service).getBytes(StandardCharsets.UTF_8);
            out.write(info);
            out.write(result.toByteArray());
            out.flush();
            return true;
        }
        return false;
    }

    /**
     * git操作通用方法
     * @param repository git仓库路径
     * @param service 服务类型
     * @param request http request
     * @param response http response
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static void server(String repository, String service, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        server(repository, service, request, response, true);
    }

    /**
     * git操作通用方法
     * @param repository git仓库路径
     * @param service 服务类型
     * @param request http request
     * @param response http response
     * @param respondInTime 是否及时响应，true 每次调用write,就会调用一次flush
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static void server(String repository, String service, HttpServletRequest request, HttpServletResponse response, boolean respondInTime) throws IOException, InterruptedException {
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-" + service + "-result");
        InputStream in = request.getInputStream();
        OutputStream out = response.getOutputStream();
        if (respondInTime){
            out = new FlushOutputStream(out);
        }

        //数据量较大时会接收到gzip格式数据
        String gzip = request.getHeader("Content-Encoding");
        in = "gzip".equals(gzip)||"x-gzip".equals(gzip)? new GZIPInputStream(in):in;

        server(repository, service, in, out);
    }

    /**
     * git操作通用方法
     * @param repository git仓库路径
     * @param service 服务类型
     * @param in 输入流，客户端发出的数据
     * @param out 输出流，返回给客户端的数据
     * @return 是否执行成功
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private static boolean server(String repository, String service, InputStream in, OutputStream out) throws IOException, InterruptedException {
        String cmd = String.format("git %s --stateless-rpc %s", service.substring(4), repository);
        int code = Commander.exec(cmd, in, out);
        //接收数据后更新库信息
        if (service.equals("git-receive-pack") && code == 0) {
            code = Commander.exec(String.format("git --git-dir %s update-server-info", repository));
        }
        return code == 0;
    }

    /**
     *
     * @param repository git仓库路径
     * @return boolean
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static boolean init(String repository) throws IOException, InterruptedException {
        File dir = new File(repository);
        if (!dir.exists()){
            String cmd = String.format("git init --bare %s", repository);
            return Commander.exec(cmd) == 0;
        }
        return false;
    }

}
