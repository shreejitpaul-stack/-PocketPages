package com.example.droiddevs

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN PAGE ADAPTER ---
class PageAdapter(
    private val onPageClick: (Page) -> Unit,
    private val onPageLongClick: (Page) -> Unit
) : ListAdapter<Page, PageAdapter.PageViewHolder>(PageDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PageViewHolder(view)
    }
    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPageTitle: TextView = itemView.findViewById(R.id.tvPageTitle)
        private val tvPagePreview: TextView = itemView.findViewById(R.id.tvPagePreview)
        private val tvPageDate: TextView = itemView.findViewById(R.id.tvPageDate)
        private val ivPageIcon: ImageView = itemView.findViewById(R.id.ivPageIcon)
        fun bind(page: Page) {
            tvPageTitle.text = page.title.ifEmpty { "Untitled" }
            tvPagePreview.text = page.blocks.firstOrNull { it.type == BlockType.TEXT && it.content.isNotEmpty() }?.content?.take(100) ?: "No additional text"
            tvPageDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(page.updatedAt)
            ivPageIcon.setImageResource(R.drawable.ic_document)
            itemView.setOnClickListener { onPageClick(page) }
            itemView.setOnLongClickListener { onPageLongClick(page); true }
        }
    }
}

class PageDiffCallback : DiffUtil.ItemCallback<Page>() {
    override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean = oldItem == newItem
}


// --- BLOCK ADAPTER ---
class BlockAdapter(
    private val onBlockContentChanged: (position: Int, content: String) -> Unit,
    private val onBlockDeleted: (position: Int) -> Unit,
    private val onCheckboxChanged: (position: Int, isChecked: Boolean) -> Unit,
    private val onRequestFocus: (position: Int, cursorPosition: Int) -> Unit,
    private val isProgrammaticUpdate: () -> Boolean,
    private val onBlockFocused: (position: Int) -> Unit
) : ListAdapter<Block, BlockAdapter.BaseBlockViewHolder>(BlockDiffCallback()) {

    object ContentUpdatePayload

    abstract class BaseBlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(block: Block)
        open fun bindPayload(block: Block, payload: Any) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBlockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (BlockType.values()[viewType]) {
            BlockType.HEADING_1 -> HeadingBlockViewHolder(inflater.inflate(R.layout.item_block_heading_1, parent, false))
            BlockType.HEADING_2 -> Heading2BlockViewHolder(inflater.inflate(R.layout.item_block_heading_2, parent, false))
            BlockType.TODO -> TodoBlockViewHolder(inflater.inflate(R.layout.item_block_todo, parent, false))
            BlockType.QUOTE -> QuoteBlockViewHolder(inflater.inflate(R.layout.item_block_quote, parent, false))
            BlockType.BULLET_LIST -> BulletListBlockViewHolder(inflater.inflate(R.layout.item_block_bullet_list, parent, false))
            else -> TextBlockViewHolder(inflater.inflate(R.layout.item_block_text, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseBlockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: BaseBlockViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bindPayload(getItem(position), payloads.first())
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    abstract inner class BaseTextBlockViewHolder(view: View) : BaseBlockViewHolder(view) {
        protected val editText: DeletableEditText = view.findViewById(R.id.etBlockContent)
        private var textWatcher: TextWatcher? = null

        init {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onBlockFocused(bindingAdapterPosition)
                }
            }
        }

        override fun bind(block: Block) {
            textWatcher?.let { editText.removeTextChangedListener(it) }

            val selection = if (editText.hasFocus()) editText.selectionStart else -1

            if (editText.text.toString() != block.content) {
                editText.setText(block.content)
                if (selection != -1) {
                    val length = editText.text?.length ?: 0
                    editText.setSelection(minOf(selection, length))
                }
            }

            editText.onDeleteKeyListener = {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val pos = bindingAdapterPosition
                    onRequestFocus(pos - 1, -1)
                    onBlockDeleted(pos)
                }
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (editText.hasFocus() && !isProgrammaticUpdate() && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        onBlockContentChanged(bindingAdapterPosition, s.toString())
                    }
                }
            }
            editText.addTextChangedListener(textWatcher)
        }

        override fun bindPayload(block: Block, payload: Any) {
            if (payload == ContentUpdatePayload) {
                if (editText.text.toString() != block.content) {
                    val selection = if (editText.hasFocus()) editText.selectionStart else -1
                    editText.setText(block.content)
                    if (selection != -1) {
                        val length = editText.text?.length ?: 0
                        editText.setSelection(minOf(selection, length))
                    }
                }
            }
        }
    }

    inner class TextBlockViewHolder(view: View) : BaseTextBlockViewHolder(view)

    inner class HeadingBlockViewHolder(view: View) : BaseTextBlockViewHolder(view) {
        override fun bind(block: Block) {
            super.bind(block)
            // Make the heading text bold
            editText.typeface = Typeface.DEFAULT_BOLD
        }
    }

    inner class Heading2BlockViewHolder(view: View) : BaseTextBlockViewHolder(view) {
        override fun bind(block: Block) {
            super.bind(block)
            // Make the heading 2 text bold
            editText.typeface = Typeface.DEFAULT_BOLD
        }
    }

    inner class QuoteBlockViewHolder(view: View) : BaseTextBlockViewHolder(view)
    inner class BulletListBlockViewHolder(view: View) : BaseTextBlockViewHolder(view)

    inner class TodoBlockViewHolder(view: View) : BaseTextBlockViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.cbTodo)

        override fun bind(block: Block) {
            super.bind(block)
            bindCheckbox(block)
        }

        override fun bindPayload(block: Block, payload: Any) {
            super.bindPayload(block, payload)
            if (payload == ContentUpdatePayload) {
                bindCheckbox(block)
            }
        }

        private fun bindCheckbox(block: Block) {
            val isCompleted = block.properties["completed"] as? Boolean ?: false
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = isCompleted
            editText.paintFlags = if (isCompleted) editText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else editText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onCheckboxChanged(bindingAdapterPosition, isChecked)
                }
            }
        }
    }
}

class BlockDiffCallback : DiffUtil.ItemCallback<Block>() {
    override fun areItemsTheSame(oldItem: Block, newItem: Block): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Block, newItem: Block): Boolean = oldItem == newItem

    override fun getChangePayload(oldItem: Block, newItem: Block): Any? {
        if (oldItem.content != newItem.content || oldItem.properties != newItem.properties) {
            return BlockAdapter.ContentUpdatePayload
        }
        return super.getChangePayload(oldItem, newItem)
    }
}