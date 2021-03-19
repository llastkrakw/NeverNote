package com.llastkrakw.nevernote


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.llastkrakw.nevernote.core.constants.IS_NOTIFICATION_TASK_EXTRA
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.getLocationOnScreen
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.setTextViewDrawableColor
import com.llastkrakw.nevernote.databinding.ActivityMainBinding
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import com.llastkrakw.nevernote.views.notes.activities.AddNoteActivity
import com.llastkrakw.nevernote.views.notes.fragments.NoteFragment
import com.llastkrakw.nevernote.views.task.fragments.AddTaskFragment
import com.llastkrakw.nevernote.views.task.fragments.TaskFragment
import java.util.*


private const val NUM_PAGES = 2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var binding: ActivityMainBinding
    private lateinit var isDark : Any

    private val noteViewModel : NoteViewModel by viewModels(){
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    companion object{
        const val ADD_NOTE_REQUEST_CODE = 10
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            data?.getParcelableExtra<Note>(AddNoteActivity.EXTRA_NOTE).let {
                if(it != null)
                    noteViewModel.insertNote(it)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = ""

        isDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK


        viewPager = binding.pager

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        toggleAction()
        viewPager.registerOnPageChangeCallback(ChangeColorCallback())

        val isTaskNotification = intent?.getBooleanExtra(IS_NOTIFICATION_TASK_EXTRA, false)

        if(isTaskNotification == true)
            viewPager.currentItem = 1

        binding.apply {
            actionNote.setOnClickListener {
                showNoteActionDialog(it)
            }

            add.setOnClickListener(){
                when(viewPager.currentItem){
                    0 -> {
                        val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
                        @Suppress("DEPRECATION")
                        startActivityForResult(intent, ADD_NOTE_REQUEST_CODE)
                    }
                    1 -> {
                        AddTaskFragment().show(supportFragmentManager, "Add task")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        noteViewModel.selectedNotes.observe(this, {

            when(it.isEmpty()){
                true -> {
                    menuInflater.inflate(R.menu.note_menu, menu)
                    invalidateOptionsMenu()
                }
                false -> {
                    menuInflater.inflate(R.menu.selection_menu, menu)
                    invalidateOptionsMenu()
                }
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_switch_layout -> {
            showMenuDialog()
            true
        }

        R.id.action_delete_note ->{
            Log.d("multi", "delete note")
            noteViewModel.deleteNotes()
            true
        }

        R.id.action_select_all_note ->{
            Log.d("multi", "selected all")
            true
        }

        R.id.action_folder_note ->{
            showFolderDialog()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun showFolderDialog(){
        val items : List<FolderWithNotes>? = noteViewModel.allFolderWithNotes.value
        val itemsString = items?.map { item -> item.folder.folderName }?.toTypedArray()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setItems(itemsString,
                DialogInterface.OnClickListener { _, selected ->
                    items?.forEach {
                        if (itemsString?.get(selected) == it.folder.folderName)
                            noteViewModel.addNotesToFolder(it)
                    }
                })

        val dialog: AlertDialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp: WindowManager.LayoutParams = dialog.window!!.attributes

        wmlp.gravity = Gravity.TOP or Gravity.END
        wmlp.x = 100 //x position

        wmlp.y = 100 //y position


        dialog.show()
        dialog.window!!.setLayout(650, 550);
    }


    private fun showNoteActionDialog(view: View){
        val items = arrayOf<CharSequence>("Trash", "New Folder")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setItems(items,
                DialogInterface.OnClickListener { _, selected ->
                    when (selected) {
                        1 -> showAddFolderDialog()
                    }
                })

        val dialog: AlertDialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp: WindowManager.LayoutParams = dialog.window!!.attributes

        wmlp.gravity = Gravity.TOP or Gravity.START
        val point = getLocationOnScreen(view)
        wmlp.x = point.x//x position

        wmlp.y = point.y //y position

        dialog.show()
        dialog.window!!.setLayout(600, 400);
    }

    private fun showAddFolderDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val folderView = layoutInflater.inflate(R.layout.add_folder, null)

        builder.setView(folderView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editText = folderView.findViewById<EditText>(R.id.add_folder_edit_text)
        val addButton = folderView.findViewById<TextView>(R.id.button_add_folder)
        val cancelButton = folderView.findViewById<TextView>(R.id.add_folder_cancel)

        addButton.setOnClickListener(){
           editText.text?.let {
               if(it.toString().isNotEmpty()){
                   val folder = Folder(null, it.toString(), Date())
                   noteViewModel.insertFolder(folder)
                   alertDialog.cancel()
               }
           }
        }

        cancelButton.setOnClickListener(){
            alertDialog.cancel()
        }

        alertDialog.show()
    }

    private fun showMenuDialog(){

        val items = arrayOf<CharSequence>("ListView", "GridView", "Setting")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setItems(items,
                DialogInterface.OnClickListener { _, selected ->
                    when (selected) {
                        1 -> noteViewModel.toggleLayoutNoteManager(true)
                        0 -> noteViewModel.toggleLayoutNoteManager(false)
                    }
                })

        val dialog: AlertDialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp: WindowManager.LayoutParams = dialog.window!!.attributes

        wmlp.gravity = Gravity.TOP or Gravity.END
        wmlp.x = 100 //x position

        wmlp.y = 100 //y position


        dialog.show()
        dialog.window!!.setLayout(650, 550);
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }


    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment{

            if(position == 0)
                return NoteFragment.newInstance()
            return TaskFragment.newInstance()
        }

    }

    private inner class ChangeColorCallback : ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            toggleAction()
            super.onPageSelected(position)
        }
    }

    private fun toggleAction(){

        val position = viewPager.currentItem

        when (isDark) {
            Configuration.UI_MODE_NIGHT_NO -> {
                if (position == 0) {
                    binding.actionTask.setTextColor(getColor(R.color.hidden_text))
                    binding.actionNote.setTextColor(getColor(R.color.black))
                    setTextViewDrawableColor(binding.actionNote, R.color.black)
                } else {
                    binding.actionTask.setTextColor(getColor(R.color.black))
                    binding.actionNote.setTextColor(getColor(R.color.hidden_text))
                    setTextViewDrawableColor(binding.actionNote, R.color.hidden_text)
                }
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                if (position == 0) {
                    binding.actionTask.setTextColor(getColor(R.color.hidden_text))
                    binding.actionNote.setTextColor(getColor(R.color.white))
                    setTextViewDrawableColor(binding.actionNote, R.color.white)
                } else {
                    binding.actionTask.setTextColor(getColor(R.color.white))
                    binding.actionNote.setTextColor(getColor(R.color.hidden_text))
                    setTextViewDrawableColor(binding.actionNote, R.color.hidden_text)
                }
            } // Night mode is active, we're using dark theme
        }
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                isDark = Configuration.UI_MODE_NIGHT_NO
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                isDark = Configuration.UI_MODE_NIGHT_YES
            } // Night mode is active, we're using dark theme
        }
    }
}