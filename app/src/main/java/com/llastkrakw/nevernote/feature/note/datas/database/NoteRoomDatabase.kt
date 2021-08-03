package com.llastkrakw.nevernote.feature.note.datas.database

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.llastkrakw.nevernote.core.constants.NOTE_DATABASE
import com.llastkrakw.nevernote.core.utilities.SpanUtils
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.spanToHtml
import com.llastkrakw.nevernote.core.converters.DateConverter
import com.llastkrakw.nevernote.core.converters.NoteConverter
import com.llastkrakw.nevernote.feature.note.datas.dao.CrossRefDao
import com.llastkrakw.nevernote.feature.note.datas.dao.FolderDao
import com.llastkrakw.nevernote.feature.note.datas.dao.NoteDao
import com.llastkrakw.nevernote.feature.note.datas.dao.RecordRefDao
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderNoteCrossRef
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.datas.entities.RecordRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Note::class, Folder::class, FolderNoteCrossRef::class, RecordRef::class], version = 1, exportSchema = false)
@TypeConverters(NoteConverter::class, DateConverter::class)
abstract class NoteRoomDatabase : RoomDatabase() {

    abstract fun noteDao() : NoteDao
    abstract fun folderDao() : FolderDao
    abstract fun crossRefDao() : CrossRefDao
    abstract fun recordRefDao() : RecordRefDao

    companion object{

        @Volatile
        var INSTANCE : NoteRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope) : NoteRoomDatabase{

            return INSTANCE ?: synchronized(this){

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteRoomDatabase::class.java,
                    NOTE_DATABASE
                )
                    .addCallback(NoteDatabaseCallback(scope))
                    .build()

                INSTANCE = instance

                instance
            }

        }
    }

    private class NoteDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.noteDao(), database.folderDao(), database.crossRefDao())
                }
            }
        }

        suspend fun populateDatabase(noteDao: NoteDao, folderDao: FolderDao, crossRefDao: CrossRefDao) {

            val folder = Folder(null, "Welcome", Date())
            val folderId : Long = folderDao.insert(folder)

            val title = "Welcome to"
            val content = "NeverNote"

            val titleSpan = SpannableString(title)
            val contentSpan = SpannableString(content)

            titleSpan.setSpan(
                StyleSpan(Typeface.BOLD),
                0, // start
                title.length, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            contentSpan.setSpan(
                StyleSpan(Typeface.NORMAL),
                0,
                content.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            val note = Note(
                null,
                spanToHtml(contentSpan),
                spanToHtml(titleSpan),
                Date(),
                null,
                Date(),
                null,
                null,
            )

            val noteId1 : Long = noteDao.insert(note)
            crossRefDao.insert(FolderNoteCrossRef(folderId.toInt(), noteId1.toInt()))

            val title2 = "Lorem ipsum"
            val content2 = "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document or a typeface without relying on meaningful content. Lorem ipsum may be used as a placeholder before final copy is available"

            val titleSpan2 = SpannableString(title2)
            val contentSpan2 = SpannableString(content2)

            titleSpan2.setSpan(
                StyleSpan(Typeface.BOLD),
                0, // start
                title2.length, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            contentSpan2.setSpan(
                StyleSpan(Typeface.NORMAL),
                0,
                content2.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            val note2 = Note(
                null,
                spanToHtml(contentSpan2),
                spanToHtml(titleSpan2),
                Date(),
                null,
                Date(),
                null,
                null
            )

            val noteId2 : Long = noteDao.insert(note2)
            crossRefDao.insert(FolderNoteCrossRef(folderId.toInt(), noteId2.toInt()))

            val title3 = "Why do we use it?"
            val content3 = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)."

            val titleSpan3 = SpannableString(title3)
            val contentSpan3 = SpannableString(content3)

            titleSpan3.setSpan(
                StyleSpan(Typeface.BOLD),
                0, // start
                title3.length, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            contentSpan3.setSpan(
                StyleSpan(Typeface.NORMAL),
                0,
                content3.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            val note3 = Note(
                null,
                spanToHtml(contentSpan3),
                spanToHtml(titleSpan3),
                Date(),
                null,
                Date(),
                null,
                null
            )

            val noteId3 : Long = noteDao.insert(note3)
            crossRefDao.insert(FolderNoteCrossRef(folderId.toInt(), noteId3.toInt()))

            val title4 = "Where can I get some?"
            val content4 = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text. All the Lorem Ipsum generators on the Internet"

            val titleSpan4 = SpannableString(title4)
            val contentSpan4 = SpannableString(content4)

            titleSpan4.setSpan(
                StyleSpan(Typeface.BOLD),
                0, // start
                title4.length, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            contentSpan4.setSpan(
                StyleSpan(Typeface.NORMAL),
                0,
                content4.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            val note4 = Note(
                null,
                spanToHtml(contentSpan4),
                spanToHtml(titleSpan4),
                Date(),
                null,
                Date(),
                null,
                null
            )

            val noteId4 : Long = noteDao.insert(note4)
            crossRefDao.insert(FolderNoteCrossRef(folderId.toInt(), noteId4.toInt()))
        }
    }

}