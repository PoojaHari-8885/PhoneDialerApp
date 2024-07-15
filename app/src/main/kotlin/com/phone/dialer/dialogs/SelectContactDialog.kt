package com.phon.dialer.dialogs

import android.graphics.Color
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.contacts.Contact
import com.simplemobiletools.commons.views.MySearchMenu
import com.phon.dialer.R
import com.phon.dialer.activities.SimpleActivity
import com.phon.dialer.adapters.ContactsAdapter
import com.phon.dialer.databinding.DialogSelectContactBinding
import java.util.Locale

class SelectContactDialog(val activity: SimpleActivity, val contacts: List<Contact>, val callback: (selectedContact: Contact) -> Unit) {
    private val binding by activity.viewBinding(DialogSelectContactBinding::inflate)

    private var dialog: AlertDialog? = null

    init {
        binding.apply {
            letterFastscroller.textColor = activity.getProperTextColor().getColorStateList()
            letterFastscrollerThumb.setupWithFastScroller(letterFastscroller)
            letterFastscrollerThumb.textColor = activity.getProperPrimaryColor().getContrastColor()
            letterFastscrollerThumb.thumbColor = activity.getProperPrimaryColor().getColorStateList()

            setupLetterFastScroller(contacts)
            configureSearchView()

            selectContactList.adapter = ContactsAdapter(activity, contacts.toMutableList(), selectContactList, allowLongClick = false) {
                callback(it as Contact)
                dialog?.dismiss()
            }
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { _, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    backPressed()
                }
                true
            }
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.choose_contact) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun setupLetterFastScroller(contacts: List<Contact>) {
        binding.letterFastscroller.setupWithRecyclerView(binding.selectContactList, { position ->
            try {
                val name = contacts[position].getNameToDisplay()
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })
    }

    private fun configureSearchView() = with(binding.contactSearchView) {
        updateHintText(context.getString(R.string.search_contacts))
        binding.topToolbarSearch.imeOptions = EditorInfo.IME_ACTION_DONE

        toggleHideOnScroll(true)
        setupMenu()
        setSearchViewListeners()
        updateSearchViewUi()
    }

    private fun MySearchMenu.updateSearchViewUi() {
        getToolbar().beInvisible()
        updateColors()
        setBackgroundColor(Color.TRANSPARENT)
        binding.topAppBarLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun MySearchMenu.setSearchViewListeners() {
        onSearchOpenListener = {
            updateSearchViewLeftIcon(R.drawable.ic_cross_vector)
        }
        onSearchClosedListener = {
            binding.topToolbarSearch.clearFocus()
            activity.hideKeyboard(binding.topToolbarSearch)
            updateSearchViewLeftIcon(R.drawable.ic_search_vector)
        }

        onSearchTextChangedListener = { text ->
            filterContactListBySearchQuery(text)
        }
    }

    private fun updateSearchViewLeftIcon(iconResId: Int) = with(binding.root.findViewById<ImageView>(R.id.top_toolbar_search_icon)) {
        post {
            setImageResource(iconResId)
        }
    }

    private fun filterContactListBySearchQuery(query: String) {
        val adapter = binding.selectContactList.adapter as? ContactsAdapter
        var contactsToShow = contacts
        if (query.isNotEmpty()) {
            contactsToShow = contacts.filter { it.name.contains(query, true) }
        }
        checkPlaceholderVisibility(contactsToShow)

        if (adapter?.contacts != contactsToShow) {
            adapter?.updateItems(contactsToShow)
            setupLetterFastScroller(contactsToShow)

            binding.selectContactList.apply {
                post {
                    scrollToPosition(0)
                }
            }
        }
    }

    private fun checkPlaceholderVisibility(contacts: List<Contact>) = with(binding) {
        contactsEmptyPlaceholder.beVisibleIf(contacts.isEmpty())

        if (contactSearchView.isSearchOpen) {
            contactsEmptyPlaceholder.text = activity.getString(R.string.no_items_found)
        }

        letterFastscroller.beVisibleIf(contactsEmptyPlaceholder.isGone())
        letterFastscrollerThumb.beVisibleIf(contactsEmptyPlaceholder.isGone())
    }

    private fun backPressed() {
        if (binding.contactSearchView.isSearchOpen) {
            binding.contactSearchView.closeSearch()
        } else {
            dialog?.dismiss()
        }
    }
}
