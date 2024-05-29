package com.centit.support.file;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public abstract class FileIOOpt {
    protected static final Logger logger = LoggerFactory.getLogger(FileIOOpt.class);

    private FileIOOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static int writeInputStreamToOutputStream(InputStream in,
                                                     OutputStream out) throws IOException {
        int read = 0;
        int length = 0;
        final byte[] bytes = new byte[1024 * 10];
        while ((read = in.read(bytes)) != -1) {
            out.write(bytes, 0, read);
            length += read;
        }
        return length;
    }

    public static int writeInputStreamToFile(InputStream in,
                                             File file) throws IOException {

        try (FileOutputStream out = new FileOutputStream(file, false)) {
            return writeInputStreamToOutputStream(in, out);
        }
    }

    public static int writeInputStreamToFile(InputStream in,
                                             String filePath) throws IOException {
        return writeInputStreamToFile(in, new File(filePath));
    }

    public static void writeStringToOutputStream(String strData, OutputStream io) throws IOException {
        try (Writer writer = new OutputStreamWriter(io)) {
            writer.write(strData);
        }
    }

    public static void writeStringToFile(String strData, File file) throws IOException {
        try (Writer writer = new FileWriter(file, false)) {
            writer.write(strData);
        }
    }

    public static void writeStringToFile(String strData, String fileName) throws IOException {
        writeStringToFile(strData, new File(fileName));
    }

    public static String readStringFromRead(Reader reader) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) != -1) {
                writer.write(buf, 0, len);
            }
            return writer.toString();
        }
    }


    /**
     * @param is 输入流
     * @return 读取的字节
     * @throws IOException 异常
     */
    public static byte[] readBytesFromInputStream(InputStream is) throws IOException {
        int len = is.available();
        if (len > 0) {
            return readBytesFromInputStream(is, len);
        } else {
            return readBytesFromInputStream(is, 32 * 1024 * 1024);
        }
    }

    /**
     * @param is     输入流
     * @param length 最大读取长度
     * @return 读取的字节
     * @throws IOException 异常
     */
    public static byte[] readBytesFromInputStream(InputStream is, int length) throws IOException {
        byte[] buf = new byte[length];
        int readed = 0;
        while (readed < length) {
            int nStep = length - readed > 1024 * 64 ? 1024 * 64 : length - readed;
            int len = is.read(buf, readed, nStep);
            if (len < 0) {
                break;
            }
            readed += len;
        }
        if (readed < 1) {
            return null;
        }
        if (readed < length) {
            byte[] buffer = new byte[readed];
            System.arraycopy(buf, 0, buffer, 0, readed);
            return buffer;
        }
        return buf;
    }

    public static byte[] readBytesFromFile(File file, int length) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return FileIOOpt.readBytesFromInputStream(fis, length);
        }
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return FileIOOpt.readBytesFromInputStream(fis, (int) file.length());
        }
    }

    public static byte[] readBytesFromFile(String filePath, int length) throws IOException {
        return FileIOOpt.readBytesFromFile(new File(filePath), length);
    }

    public static byte[] readBytesFromFile(String filePath) throws IOException {
        return FileIOOpt.readBytesFromFile(new File(filePath));
    }

    public static String readStringFromInputStream(InputStream is, String charsetName) throws IOException {
        return readStringFromRead(new InputStreamReader(is, charsetName));
    }

    public static String readStringFromInputStream(InputStream is) throws IOException {
        return readStringFromRead(new InputStreamReader(is));
    }

    public static String readStringFromFile(File file, String charsetName) throws IOException {
        return readStringFromRead(new InputStreamReader(new FileInputStream(file), charsetName));
    }

    public static String readStringFromFile(File file) throws IOException {
        return readStringFromRead(new InputStreamReader(new FileInputStream(file)));
    }

    public static String readStringFromFile(String fileName, String charsetName) throws IOException {
        return readStringFromFile(new File(fileName), charsetName);
    }

    public static String readStringFromFile(String fileName) throws IOException {
        return readStringFromFile(new File(fileName));
    }

    public static void writeObjectToFile(Object obj, String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(obj);
        }
    }

    public static Object readObjectFromFile(String fileName)
        throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return ois.readObject();
        }
    }

    public static void writeObjectAsJsonToFile(Object obj, String fileName) throws IOException {
        String sjson = JSON.toJSONString(obj);
        writeStringToFile(sjson, fileName);
    }

    public static void writeBytesToFile(byte[] bytes, String fileName) throws IOException {
        writeBytesToFile(bytes, new File(fileName));
    }

    public static void writeBytesToFile(byte[] bytes, File file) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(bytes);
        }
    }

    public static <T> T readObjectAsJsonFromFile(String fileName, Class<T> clazz)
        throws IOException {
        String sjson = readStringFromFile(fileName);
        return JSON.parseObject(sjson, clazz);
    }

    public static InputStream castObjectToInputStream(Object data){
        if(data == null) {
            return null;
        }
        if (data instanceof InputStream) {
            return (InputStream)data;
        }
        if (data instanceof ByteArrayOutputStream) {
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) data;
            return new ByteArrayInputStream(outputStream.toByteArray());
        }

        if(data instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) data);
        }

        if(data instanceof String){
            return new ByteArrayInputStream(((String) data).getBytes(StandardCharsets.UTF_8));
        }
        String dataStr = JSON.toJSONString(data);
        return new ByteArrayInputStream(dataStr.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * close the IO stream.
     *
     * @param closeable closeable
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
