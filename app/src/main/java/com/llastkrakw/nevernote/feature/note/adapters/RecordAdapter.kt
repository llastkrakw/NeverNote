package com.llastkrakw.nevernote.feature.note.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chibde.visualizer.LineBarVisualizer
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.getAudioFileContentUri
import com.llastkrakw.nevernote.core.constants.isQPlus
import com.llastkrakw.nevernote.core.extension.getColoredDrawable
import com.llastkrakw.nevernote.core.extension.getFormattedDuration
import com.llastkrakw.nevernote.core.extension.showErrorToast
import com.llastkrakw.nevernote.feature.note.datas.entities.Recording
import java.util.*


class RecordAdapter : ListAdapter<Recording, RecordAdapter.RecordViewHolder>(RecordComparator()) {

    private var player: MediaPlayer? = null
    private var triggerRecording : Recording? = null
    private var nowPLayingToggle : ImageButton? = null
    private var lineBarVisualizerBoxNowPlaying  : LinearLayout? = null
    private var recordTitleNowPlaying : TextView? = null
    private var recordDurationNowPlaying : TextView? = null
    private var progressTimer = Timer()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        return createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
    }

    private fun createViewHolder(parent: ViewGroup): RecordViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_item, parent, false)
        return RecordViewHolder(view)
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val recordTitle : TextView = itemView.findViewById(R.id.record_title)
        private val recordDuration : TextView = itemView.findViewById(R.id.record_duration)
        private val lineBarVisualizer  : LineBarVisualizer = itemView.findViewById(R.id.visualizer)
        private val lineBarVisualizerBox  : LinearLayout = itemView.findViewById(R.id.visualizer_box)
        val recordToggle : ImageButton = itemView.findViewById(R.id.play_toggle)


        fun bind(recording: Recording){

            lineBarVisualizer.setColor(ContextCompat.getColor(itemView.context, R.color.secondaryColor))
            lineBarVisualizer.setDensity(70F)

            recordTitle.text = recording.title
            recordDuration.text = recording.duration.getFormattedDuration()
            recordToggle.setOnClickListener {
                if (triggerRecording != null){
                    Log.d("record", "play")
                    if(getIsPlaying() && triggerRecording!!.id == recording.id){
                        togglePlayPause()
                        toggleVisibility()
                        Log.d("record", "toggle play")
                    }
                    else{
                        Log.d("record", "new play")
                        nowPLayingToggle!!.setImageDrawable(getToggleButtonIcon(false, itemView.context))
                        nowPLayingToggle = it as ImageButton
                        lineBarVisualizerBoxNowPlaying = lineBarVisualizerBox
                        recordTitleNowPlaying = recordTitle
                        recordDurationNowPlaying = recordDuration
                        playRecording(recording)
                        recordToggle.setImageDrawable(getToggleButtonIcon(true, itemView.context))
                    }
                }
                else{
                    Log.d("record", "first play")
                    nowPLayingToggle = it as ImageButton
                    lineBarVisualizerBoxNowPlaying = lineBarVisualizerBox
                    recordTitleNowPlaying = recordTitle
                    recordDurationNowPlaying = recordDuration
                    playRecording(recording)
                    toggleVisibility()
                    recordToggle.setImageDrawable(getToggleButtonIcon(true, itemView.context))
                }
                triggerRecording = recording
                player?.let { player -> lineBarVisualizer.setPlayer(player.audioSessionId) }

            }
        }

       private fun playRecording(recording: Recording) {

           player!!.apply {
               resetProgress()
               reset()

               try {
                   if (isQPlus()) {
                       setDataSource(itemView.context, getAudioFileContentUri(recording.id.toLong()))
                   } else {
                       setDataSource(recording.path)
                   }
               } catch (e: Exception) {
                   itemView.context?.showErrorToast(e)
                   return
               }

               try {
                   prepareAsync()
               } catch (e: Exception) {
                   itemView.context.showErrorToast(e)
                   return
               }
           }

       }



       private fun togglePlayPause() {
           if (getIsPlaying()) {
               pausePlayback()
           } else {
               resumePlayback()
           }
       }

       private fun pausePlayback() {
           player?.pause()
           recordToggle.setImageDrawable(getToggleButtonIcon(false, itemView.context))
       }

       private fun resumePlayback() {
           player?.start()
           recordToggle.setImageDrawable(getToggleButtonIcon(true, itemView.context))
       }

    }


    class RecordComparator : DiffUtil.ItemCallback<Recording>() {
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Recording,
            newItem: Recording
        ): Boolean {
            return oldItem.id == newItem.id
        }


    }

    private fun getIsPlaying() = player?.isPlaying == true

    private fun getToggleButtonIcon(isPlaying: Boolean, context: Context): Drawable {
        val drawable = if (isPlaying) R.drawable.ic_round_pause_24 else R.drawable.ic_round_play_arrow_24
        return context.resources.getColoredDrawable(drawable, R.color.white, context = context)
    }

    private fun toggleVisibility(){
        if(getIsPlaying()){
            recordTitleNowPlaying?.visibility = View.GONE
            lineBarVisualizerBoxNowPlaying?.visibility = View.VISIBLE
        }
        else{
            recordTitleNowPlaying?.visibility = View.VISIBLE
            lineBarVisualizerBoxNowPlaying?.visibility = View.GONE
        }
    }

    private fun initMediaPlayer(context: Context) {
        player = MediaPlayer().apply {
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setOnCompletionListener {
                nowPLayingToggle!!.setImageDrawable(getToggleButtonIcon(false, context))
                toggleVisibility()
            }

            setOnPreparedListener {
                if(!getIsPlaying()){
                    player?.start()
                    setupProgressTimer()
                }
                toggleVisibility()
            }
        }
    }


    private fun setupProgressTimer() {
        progressTimer.cancel()
        progressTimer = Timer()
        progressTimer.scheduleAtFixedRate(getProgressUpdateTask(), 1000, 1000)
    }


    private fun getProgressUpdateTask() = object : TimerTask() {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                if (player != null) {
                    val progress = Math.round(player!!.currentPosition / 1000.toDouble()).toInt()
                    updateCurrentProgress(progress)
                }
            }
        }
    }

    private fun updateCurrentProgress(seconds: Int) {
        recordDurationNowPlaying?.text = seconds.getFormattedDuration()
    }

    private fun resetProgress() {
        updateCurrentProgress(0)
    }

    override fun onViewAttachedToWindow(holder: RecordViewHolder) {
        super.onViewAttachedToWindow(holder)
        initMediaPlayer(holder.itemView.context)
    }

    override fun onViewDetachedFromWindow(holder: RecordViewHolder) {
        super.onViewDetachedFromWindow(holder)
        player?.stop()
        player?.release()

        player = null
    }


}