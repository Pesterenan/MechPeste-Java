package com.pesterenan.updater;

import com.pesterenan.views.InstallKrpcDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KrpcInstaller {
    static final int BUFFER = 2048;
    private static final String KRPC_GITHUB_LINK = "https://github.com/krpc/krpc/releases/download/v0.5.4/krpc-0.5.4.zip";
    private static String KSP_FOLDER = null;

    public static String getKspFolder() {
        return KSP_FOLDER;
    }

    public static void setKspFolder(String folderPath) {
        KSP_FOLDER = folderPath;
    }

    public static void downloadAndInstallKrpc() {
        InstallKrpcDialog.setStatus("Fazendo download do KRPC pelo Github...");
        downloadKrpc();
        InstallKrpcDialog.setStatus("Download completo!");
        InstallKrpcDialog.setStatus("Extraindo arquivos...");
        unzipKrpc();
        InstallKrpcDialog.setStatus("Instalação terminada, reinicie o KSP!");
    }

    public static void downloadKrpc() {
        URL krpcLink;
        try {
            krpcLink = new URL(KRPC_GITHUB_LINK);
            ReadableByteChannel readableByteChannel = Channels.newChannel(krpcLink.openStream());
            FileOutputStream fos = new FileOutputStream(KSP_FOLDER + "\\krpc-0.5.4.zip");
            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fos.close();
        } catch (IOException e) {
            InstallKrpcDialog.setStatus("Não foi possível fazer o download do KRPC");
        }
    }

    public static void unzipKrpc() {
        try {
            File folder = new File(KSP_FOLDER);
            if (!folder.exists()) {
                folder.mkdir();
            }
            BufferedOutputStream dest;
            // zipped input
            FileInputStream fis = new FileInputStream(KSP_FOLDER + "\\krpc-0.5.4.zip");
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte[] data = new byte[BUFFER];
                String fileName = entry.getName();
                File newFile = new File(folder + File.separator + fileName);
                // If directory then just create the directory (and parents if required)
                if (entry.isDirectory()) {
                    if (!newFile.exists()) {
                        newFile.mkdirs();
                    }
                } else {
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(newFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
                zis.closeEntry();
            }
            zis.close();
        } catch (Exception e) {
            InstallKrpcDialog.setStatus("Não foi possível fazer instalar o KRPC");
        }
    }
}
