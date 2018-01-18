package com.czt.mp3recorder.util;

import java.io.File;
import java.io.IOException;

import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.mj.tools.TimeUtils;




import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

public class MP3Recorder {
	//=======================AudioRecord Default Settings=======================
	private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
	/**
	 * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
	 */
	private static final int DEFAULT_SAMPLING_RATE = 11025;//模拟器仅支持从麦克风输入8kHz采样率
	private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	/**
	 * 下面是对此的封装
	 * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	 */
	private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;
	
	//======================Lame Default Settings=====================
	private static final int DEFAULT_LAME_MP3_QUALITY = 9;
	/**
	 * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
	 */
	private static final int DEFAULT_LAME_IN_CHANNEL = 1;
	/**
	 *  Encoded bit rate. MP3 file will be encoded with bit rate 96kbps 
	 */ 
	private static final int DEFAULT_LAME_MP3_BIT_RATE = 16;
	
	//==================================================================
	
	/**
	 * 自定义 每160帧作为一个周期，通知一下需要进行编码
	 */
	private static final int FRAME_COUNT = 160;
	
	/**
	 * 自定义 录音有效时长 1000ms
	 */
	private static final int AUDIO_VALID_TIME = 1000;
	private AudioRecord mAudioRecord = null;
	private int mBufferSize;
	private short[] mPCMBuffer;
	private DataEncodeThread mEncodeThread;
	private boolean mIsRecording = false;
	private boolean mCanCelRecording = false;
	//文件路径
	private File mRecordFile;
	//文件夹路径
    private String FolderPath;
	private OnAudioStatusUpdateListener audioStatusUpdateListener;
	
	private long startTime;
    private long endTime;
	/**
	 * Default constructor. Setup recorder with default sampling rate 1 channel,
	 * 16 bits pcm
	 * @param recordFile target file
	 */
	public MP3Recorder(File recordFile) {
		
		mRecordFile = recordFile;
	}
	
	/**
     * 文件存储默认sdcard/record
     */
    public MP3Recorder(){

        //默认保存路径为/sdcard/record/下
        this(Environment.getExternalStorageDirectory()+ File.separator + ConstVar.CAPTION + "_audiorecord" + File.separator);
    }

    public MP3Recorder(String filePath) {

        File path = new File(filePath);
        if(!path.exists())
            path.mkdirs();

        this.FolderPath = filePath;
        this.mRecordFile = new File(this.FolderPath, "record.mp3");
    }

	/**
	 * Start recording. Create an encoding thread. Start record from this
	 * thread.
	 * 
	 * @throws IOException  initAudioRecorder throws
	 */
	public void start() throws IOException {
		if (mIsRecording) {
			return;
		}
		mIsRecording = true; // 提早，防止init或startRecording被多次调用
		mCanCelRecording = false;
		release();
	    initAudioRecorder();
	    try {
	    	mAudioRecord.startRecording();
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("startRecording err = " + e.toString());
		}
		
		/* 获取开始时间* */
        startTime = System.currentTimeMillis();
		new Thread() {
			@Override
			public void run() {
				//设置线程权限
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				while (mIsRecording && null != mAudioRecord) {
					int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
					if (readSize > 0) {
						mEncodeThread.addTask(mPCMBuffer, readSize);
						calculateRealVolume(mPCMBuffer, readSize);
					}
				}
				// release and finalize audioRecord
				release();
				
				/* 获取结束时间* */
		        endTime = System.currentTimeMillis();
				// stop the encoding thread and try to wait
				// until the thread finishes its job
				mEncodeThread.sendStopMessage();
				if (mCanCelRecording) {
					audioStatusUpdateListener.onCancel();
				} else {
					if(endTime - startTime > AUDIO_VALID_TIME){
						audioStatusUpdateListener.onStop(mRecordFile.toString());
					} else{
						audioStatusUpdateListener.onFailed();
					}
				}
			}
			/**
			 * 此计算方法来自samsung开发范例
			 * 
			 * @param buffer buffer
			 * @param readSize readSize
			 */
			private void calculateRealVolume(short[] buffer, int readSize) {
				double sum = 0;
				for (int i = 0; i < readSize; i++) {  
				    // 这里没有做运算的优化，为了更加清晰的展示代码  
				    sum += buffer[i] * buffer[i]; 
				} 
				if (readSize > 0) {
					double amplitude = sum / readSize;
					mVolume = (int) Math.sqrt(amplitude);
				}
			}
		}.start();
	}
	private int mVolume;

	/**
	 * 获取真实的音量。 [算法来自三星]
	 * @return 真实音量
     */
	public int getRealVolume() {
		return mVolume;
	}

	/**
	 * 获取相对音量。 超过最大值时取最大值。
	 * @return 音量
     */
	public int getVolume(){
		if (mVolume >= MAX_VOLUME) {
			return MAX_VOLUME;
		}
		return mVolume;
	}
	private static final int MAX_VOLUME = 2000;

	/**
	 * 根据资料假定的最大值。 实测时有时超过此值。
	 * @return 最大音量值。
     */
	public int getMaxVolume(){
		return MAX_VOLUME;
	}
	public void stop(){
		mIsRecording = false;
	}
	public void cancel(){
		mIsRecording = false;
		mCanCelRecording = true;
	}
	public void release(){
		try {
			if(null != mAudioRecord){
				mAudioRecord.stop();
				mAudioRecord.release();
				mAudioRecord = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("release mAudioRecord err = " + e.toString());
		}
	}
	public boolean isRecording() {
		return mIsRecording;
	}
	/**
	 * Initialize audio recorder
	 */
	private void initAudioRecorder() throws IOException {
		mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
				DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());
		
		int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
		/* Get number of samples. Calculate the buffer size 
		 * (round up to the factor of given frame size) 
		 * 使能被整除，方便下面的周期性通知
		 * */
		int frameSize = mBufferSize / bytesPerFrame;
		if (frameSize % FRAME_COUNT != 0) {
			frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
			mBufferSize = frameSize * bytesPerFrame;
		}
		
		/* Setup audio recorder */
		mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
				DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(),
				mBufferSize);
		
		mPCMBuffer = new short[mBufferSize];
		/*
		 * Initialize lame buffer
		 * mp3 sampling rate is the same as the recorded pcm sampling rate 
		 * The bit rate is 96kbps
		 * 
		 */
		Debugs.debug("initAudioRecorder LameUtil.init 1");
		LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
		// Create and run thread used to encode data
		// The thread will 
//		mRecordFile = new File(FolderPath,TimeUtils.getCurrentTime() + ".mp3");
//		mRecordFile = new File(FolderPath,"record.mp3");
		Debugs.debug("initAudioRecorder.mRecordFile = " + mRecordFile.toString());
		mEncodeThread = new DataEncodeThread(mRecordFile, mBufferSize);
		mEncodeThread.start();
		mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
		mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
	}
	
	public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }
	
	public interface OnAudioStatusUpdateListener {
        /**
         * 停止录音
         * @param filePath 保存路径
         */
        public void onStop(String filePath);
        /**
         * 录音失败
         */
        public void onFailed();
        /**
         * 取消录音
         */
        public void onCancel();
    }
}