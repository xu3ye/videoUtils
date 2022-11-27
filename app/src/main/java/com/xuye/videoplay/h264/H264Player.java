package com.xuye.videoplay.h264;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by xu_ye.
 * Date: 2022/11/20 21:31
 */
public class H264Player implements Runnable {
    String TAG = "H264Player---tag---";
    String path;
    MediaCodec mediaCodec;

    public H264Player(Surface surface, String path) {
        try {
            this.path = path;

            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);//注意这个是解码createDecoderByType   这个是createEncoderByType编码，错误排除老久都没注意这个
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 364, 368);//宽高虽然解码没用，但似乎得实是2的倍数
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(mediaFormat, surface, null, 0);
        } catch (Exception e) {
//            Log.d(TAG, "不支持： " + e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "支持");
    }

    public void player() {
        Log.d(TAG, "运行player");
        mediaCodec.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, "运行player");
            decodeH264(path);
        } catch (Exception e) {
            Log.d(TAG, "运行" + e.getMessage());

//            e.printStackTrace();
        }
    }

    /**
     * 解析h264数据
     *
     * @param path 文件路径
     */
    private void decodeH264(String path) {
        Log.d(TAG, "解析h264数据   ");
        byte[] dataBytes;
        int index;//容器的索引
        int startIndex = 0;
        int nextIndex;
        try {
            dataBytes = getBytes(path);
            while (true) {
                nextIndex = findByFrame(dataBytes, startIndex+2);//不能为0或者1，因为开头有可能就是分隔符
                index = mediaCodec.dequeueInputBuffer(10000);//获取空容器的索引，时间参数的意思是如果没有空容器可以等待多长时间
                if (index > 0) {
                    ByteBuffer byteBuffers = mediaCodec.getInputBuffer(index);//拿到容器
                    int length = nextIndex - startIndex;//一帧的长度
//                    Log.d(TAG, "数据输入   " + length);
                    if (byteBuffers != null) {
                        byteBuffers.put(dataBytes, startIndex, length);//往容器里放一帧数据
                        //索引 偏移量， 数据长度， 帧间隔时间（解码不需要，由流里面的时间参数决定） 标志位
                        mediaCodec.queueInputBuffer(index, 0, length, 0, 0);//把容器的索引给dsp
                    }
                    startIndex = nextIndex;
                }

                int outIndex = mediaCodec.dequeueOutputBuffer(new MediaCodec.BufferInfo(), 1000);//查询解码是否成功，返回值有索引代表成功
                if (outIndex > 0) {
                    mediaCodec.releaseOutputBuffer(outIndex, true);//render为true可以将出来的数据直接渲染到surface上
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件转字节流
     *
     * @param path 文件路径
     * @return 文件字节数组
     */
    private byte[] getBytes(String path) throws IOException {
        Log.d(TAG, "读取文件  ");
        FileInputStream fileInputStream = new FileInputStream(path);//创建文件实例
        InputStream is = new DataInputStream(fileInputStream);//为了快速读取文件
        int len = 0;
        int size = 1024;
        byte[] buf;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        len = is.read(buf, 0, size);
        while (len != -1) {//检查是否读到文件末尾
//            Log.d(TAG, is.available() + "  " + len);
            bos.write(buf, 0, len);//为什么读1024个字节
            len = is.read(buf, 0, size);//不能读到-1
        }
        buf = bos.toByteArray();
        if (fileInputStream != null)
            fileInputStream.close();
        if (is != null)
            is.close();
        return buf;
    }

    /**
     * @param bytes 整个h264流数组
     * @param start 开始位置
     * @return 返回一帧的长度
     */
    private int findByFrame(byte[] bytes, int start) {
        for (int i = start; i < bytes.length - 4; i++) {
            boolean isEnd = bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01;
            boolean isEnd1 = bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 3] == 0x01;//分隔符有00 00 00 01和00 00 01两种
            if (isEnd || isEnd1) {
                return i;
            }
        }
        return -1;
    }
}


