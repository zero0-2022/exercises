package com.test.mycat;

import com.test.servlet.MyServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyCat 功能实现
 */
public class MyCatServer {
    // key 为 servlet 的类名，value 为对应 servlet 实例对象
    private final Map<String, MyServlet> nameServletMap = new ConcurrentHashMap<>();
    // key 为 servlet 的类名，value 为对应 servlet 类的全限定性类名
    private final Map<String, String> nameServletClassMap = new HashMap<>();
    // 静态资源缓存
    private final Map<String, String> staticSources = new HashMap<>();
    // MyServlet 存放位置
    private final String basePackagePath;
    // 静态资源 存放位置
    private final String staticSourcePath;

    public MyCatServer(String path, String staticSourcePath) {
        this.basePackagePath = path;
        this.staticSourcePath = staticSourcePath;
    }

    // 启动 MyCat 服务
    public void start(){
        cacheClassesName(this.basePackagePath);
        cacheStaticName(this.staticSourcePath);
        startServer();
    }

    private static File getFileDir(String path) {
        ClassLoader classLoader = MyCatServer.class.getClassLoader();
        URL url = classLoader.getResource(path.replaceAll("\\.", "/"));
        if (url == null){
            return null;
        }
        return new File(url.getFile());
    }

    private void cacheClassesName(String path){
        File dir = getFileDir(path);
        if (dir == null) {
            return;
        }
        // 循环遍历指定包及子包的所有文件，查找所有 .class 文件
        for (File f: Objects.requireNonNull(dir.listFiles())) {
            String name = f.getName();
            if (f.isDirectory()) { // 若当前遍历的 f 是目录，递归继续处理
                cacheClassesName(path + "." + name);
            } else if (name.endsWith(".class")) {
                String className = name.replace(".class", "").trim();
                nameServletClassMap.put(className.toLowerCase(), path + "." + className);
            }
        }
    }

    private void cacheStaticName(String path) {
        // 将 url 资源转为 File 资源
        File dir = new File(path);
        Path p = Paths.get(path);
        // 循环遍历指定包及子包的所有文件，查找所有 .class 文件
        for (File f: Objects.requireNonNull(dir.listFiles())) {
            String name = f.getName();
            if (f.isDirectory()) { // 若当前遍历的 f 是目录，递归继续处理
                cacheStaticName(p.resolve(name).toString());
            } else {
                staticSources.put(name, p.resolve(name).toString());
            }
        }
    }

    private void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bs = new ServerBootstrap();

        try {
            bs.group(bossGroup, workGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new MyCatHandler(nameServletMap, nameServletClassMap, staticSources));
                        }
                    });
            int port = getServerPort();
            ChannelFuture future = bs.bind(port).sync();
            System.out.println("MyCat started at port: " + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private static int getServerPort() throws DocumentException {
        InputStream in = MyCatServer.class.getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();
        Document doc = saxReader.read(in);
        Element ele = (Element) doc.selectSingleNode("//port");
        return Integer.parseInt(ele.getTextTrim());
    }
}
