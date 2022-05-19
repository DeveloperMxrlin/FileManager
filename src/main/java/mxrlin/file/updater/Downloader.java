/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.updater;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.plugin.Plugin;

import java.io.*;

public class Downloader {

    private Plugin plugin;
    private int spigotID;
    private File destination;

    public Downloader(Plugin plugin, int spigotID, File destination){

        this.plugin = plugin;
        this.spigotID = spigotID;
        this.destination = destination;

        if(!destination.exists()){
            if(destination.isDirectory()) destination.mkdir();
            else if(destination.isFile()) {
                try {
                    destination.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void download() throws Exception {

        String url = "https://api.spiget.org/v2/resources/" + spigotID + "/download";

        Request request = (new Request.Builder()).url(url).header("User-Agent", "FileManager").build();
        Response response = (new OkHttpClient()).newCall(request).execute();
        ResponseBody body = null;

        try{

            if(response.code() != 200){
                throw new Exception("Download Error for " + plugin.getName() + " code: " + response.code() + " message: " + response.message() + " url: " + url);
            }

            body = response.body();
            if (body == null)
                throw new Exception("Download of '" + plugin.getName() + "' failed because of null response body!");
            else if (body.contentType() == null)
                throw new Exception("Download of '" + plugin.getName() + "' failed because of null content type!");
            else if (!body.contentType().type().equals("application"))
                throw new Exception("Download of '" + plugin.getName() + "' failed because of invalid content type: " + body.contentType().type());

            BufferedInputStream inputStream = new BufferedInputStream(body.byteStream());
            FileOutputStream fileOutputStream = new FileOutputStream(destination);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1024);
            byte[] data = new byte[1024];
            long downloadedFileSize = 0;

            int x = 0;
            while ((x = inputStream.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;

                bufferedOutputStream.write(data, 0, x);
            }

            bufferedOutputStream.close();
            inputStream.close();
            body.close();
            response.close();

        }catch (Exception e){
            if(body != null) body.close();
            response.close();
            throw e;
        }

    }

}
