package com.llastkrakw.nevernote.core.utilities

import android.text.*
import android.text.method.BaseMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.widget.EditText
import androidx.core.text.toHtml
import androidx.core.text.toSpannable
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import org.jetbrains.annotations.NotNull
import java.util.*


class Editor(@NotNull private var editText: EditText){

    private var size : Float = 2.0f
    private var state : State
    private var flag : Boolean = false
    var history : Stack<Memento> = Stack()
    var historyBack : Stack<Memento> = Stack()
    var index : Int = 0

    init {
        state = State(editText.text)
        editText.addTextChangedListener(Watcher())
    }

    private fun onEditableChanged(s: Editable?) {}

    private fun onTextChanged(s: Editable?) {}

    fun saveState(start: Int, isAdd: Boolean) : Memento {
        return Memento(state, start, isAdd)
    }

    fun restoreState(memento: Memento){
        state = memento.state
    }

    /*
    *
    *  Clear history
    *
    * */

    fun clearHistory(){
        history.clear()
        historyBack.clear()
    }


    /*
    *
    *  Undo
    *
    * */

    fun undoIsEnable() : Boolean{
        return !history.isEmpty()
    }

    fun undo(){
        if (history.isEmpty()) return
        flag = true

        val memento = history.pop()
        historyBack.push(memento)

        Log.d("Memento", "memento undo value : ${memento.state.content}")

        memento.let {
            if(it.isAdd){
                state.content.delete(it.startCursor, it.startCursor + it.state.content.length)
                editText.setSelection(it.startCursor, it.startCursor)
            }
            else{
                state.content.insert(it.startCursor, it.state.content)
                if (it.endCursor == it.startCursor) {
                    editText.setSelection(it.startCursor + it.state.content.length)
                } else {
                    editText.setSelection(it.startCursor, it.endCursor)
                }
            }
        }

        flag = false
        if (!history.empty() && history.peek().index == memento.index)
            undo()
    }


    /*
    *
    *  Redo
    *
    * */

    fun redoIsEnable() : Boolean{
        return !historyBack.isEmpty()
    }


    fun redo(){
        if (historyBack.empty()) return
        flag = true

        val memento = historyBack.pop()
        history.push(memento)

        memento.let {
            if (it.isAdd){
                state.content.insert(it.startCursor, it.state.content)
                if (it.endCursor == it.startCursor) {
                    editText.setSelection(it.startCursor + it.state.content.length)
                } else {
                    editText.setSelection(it.startCursor, it.endCursor)
                }
            }
            else {
                state.content.delete(it.startCursor, it.startCursor + it.state.content.length)
                editText.setSelection(it.startCursor, it.startCursor)
            }
            flag = false;
        }

        if (!historyBack.empty() && historyBack.peek().index == memento.index)
            redo()
    }


    /*
    *
    *  helpers
    *
    * */

    inner class Watcher : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (flag) return
            val end = start + count

            if (s != null) {
                if (end > start && end <= s.length){
                    val charSequence = s.subSequence(start, end)

                    if(charSequence.isNotEmpty()){
                        val memento = Memento(State(charSequence as Editable), start, false)
                        if (count > 1) {
                            memento.setSelectCount(count);
                        } else if (count == 1 && count == after) {
                            memento.setSelectCount(count);
                        }

                        history.push(memento)
                        historyBack.clear()
                        memento.setMementoIndex(++index)
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (flag) return
            val end = start + count

            if (s != null) {
                if (end > start){
                    val charSequence = s.subSequence(start, end)

                    if(charSequence.isNotEmpty()){
                        val memento = Memento(State(charSequence as Editable), start, true)

                        history.push(memento)
                        historyBack.clear()

                        if (before > 0) {
                            memento.setMementoIndex(index)
                        } else {
                            memento.setMementoIndex(++index)
                        }
                    }
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (flag) return;
            if (s != state.content) {
                if (s != null) {
                    state.content = s
                }
                onEditableChanged(s)
            }
            onTextChanged(s)
        }

    }
    inner class State(var content: Editable)
    inner class Memento(var state: State, var startCursor: Int, var isAdd: Boolean){

        var endCursor : Int = startCursor
        var index : Int = 0

        fun setSelectCount(count: Int) {
            endCursor += count
        }

        fun setMementoIndex(index: Int){
            this.index = index
        }
    }


    /*
    *
    *  style
    *
    * */

   fun makeBold(){
       if (!verifySelection())
            bold(state.content.toSpannable(), editText)
   }

   fun makeItalic(){
       if (!verifySelection())
            italic(state.content.toSpannable(), editText)
   }

   fun makeUnderline(){
       if (!verifySelection())
            underline(state.content.toSpannable(), editText)
   }

   fun increaseSize(){
       if (!verifySelection())
            size(size + 2.5f, state.content.toSpannable(), editText)
   }

   fun setColor(color: Int){
       if (!verifySelection())
            setColor(color, state.content.toSpannable(), editText)
   }

   fun setUrl(url: CharSequence){
       val string = SpannableString(state.content)
       string.setSpan(URLSpan(url.toString()), editText.selectionStart, editText.selectionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
       editText.setText(TextUtils.concat(string, " "))
   }

   fun alignRight(){
       spanAlignmentRight(editText)
   }

   fun alignLeft(){
       spanAlignmentLeft(editText)
   }

   private fun verifySelection() : Boolean{
       return editText.selectionEnd == editText.selectionStart
   }

}