package com.dci.dev.coffeegram.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dci.dev.coffeegram.R
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import java.util.*

class HomeFragment : Fragment() {

	private lateinit var homeViewModel: HomeViewModel

	private val calendar = Calendar.getInstance()
	private var currentMonth = 0

	private lateinit var myCalendarViewManager: CalendarViewManager
	private lateinit var mySelectionManager: CalendarSelectionManager
	private lateinit var myCalendarChangesObserver : CalendarChangesObserver

	private lateinit var tvDate: TextView
	private lateinit var tvDay: TextView
	private lateinit var tvQuote: TextView
	private lateinit var btnRight: Button
	private lateinit var btnLeft: Button

	@SuppressLint("SetTextI18n")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		homeViewModel =
			ViewModelProvider(this).get(HomeViewModel::class.java)
		val root = inflater.inflate(R.layout.fragment_home, container, false)

		tvDate = root.findViewById(R.id.tvDate)
		tvDay = root.findViewById(R.id.tvDay)
		tvQuote = root.findViewById(R.id.tvQuote)
		btnRight = root.findViewById(R.id.btnRight)
		btnLeft = root.findViewById(R.id.btnLeft)

		tvQuote.text = "A mathematician is a device \n for turning coffee into theorems."

		// set current date to calendar and current month to currentMonth variable
		calendar.time = Date()
		currentMonth = calendar[Calendar.MONTH]

		tvDate.text = "${DateUtils.getMonthName(calendar.time)}, ${DateUtils.getDayNumber(calendar.time)} "
		tvDay.text = DateUtils.getDayName(calendar.time)

		// calendar view manager is responsible for our displaying logic
		myCalendarViewManager = object :
			CalendarViewManager {
			override fun setCalendarViewResourceId(
				position: Int,
				date: Date,
				isSelected: Boolean
			): Int {
				// set date to calendar according to position where we are
				val cal = Calendar.getInstance()
				cal.time = date
				// if item is selected we return this layout items
				// in this example. monday, wednesday and friday will have special item views and other days
				// will be using basic item view
				return if (isSelected)
					when (cal[Calendar.DAY_OF_WEEK]) {
						Calendar.MONDAY -> R.layout.first_special_selected_calendar_item
						Calendar.WEDNESDAY -> R.layout.second_special_selected_calendar_item
						Calendar.FRIDAY -> R.layout.third_special_selected_calendar_item
						else -> R.layout.selected_calendar_item
					}
				else
				// here we return items which are not selected
					when (cal[Calendar.DAY_OF_WEEK]) {
						Calendar.MONDAY -> R.layout.first_special_calendar_item
						Calendar.WEDNESDAY -> R.layout.second_special_calendar_item
						Calendar.FRIDAY -> R.layout.third_special_calendar_item
						else -> R.layout.calendar_item
					}

				// NOTE: if we don't want to do it this way, we can simply change color of background
				// in bindDataToCalendarView method
			}

			override fun bindDataToCalendarView(
				holder: SingleRowCalendarAdapter.CalendarViewHolder,
				date: Date,
				position: Int,
				isSelected: Boolean
			) {
				// using this method we can bind data to calendar view
				// good practice is if all views in layout have same IDs in all item views
				holder.itemView.findViewById<TextView>(R.id.tv_date_calendar_item).text = DateUtils.getDayNumber(date)
				holder.itemView.findViewById<TextView>(R.id.tv_day_calendar_item).text = DateUtils.getDay3LettersName(date)

			}
		}

		// using calendar changes observer we can track changes in calendar
		myCalendarChangesObserver = object :
			CalendarChangesObserver {
			// you can override more methods, in this example we need only this one
			@SuppressLint("SetTextI18n")
			override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
				tvDate.text = "${DateUtils.getMonthName(date)}, ${DateUtils.getDayNumber(date)} "
				tvDay.text = DateUtils.getDayName(date)
				super.whenSelectionChanged(isSelected, position, date)
			}


		}

		// selection manager is responsible for managing selection
		mySelectionManager = object : CalendarSelectionManager {
			override fun canBeItemSelected(position: Int, date: Date): Boolean {
				// set date to calendar according to position
				val cal = Calendar.getInstance()
				cal.time = date
				// in this example sunday and saturday can't be selected, others can
				return when (cal[Calendar.DAY_OF_WEEK]) {
					Calendar.SATURDAY -> false
					Calendar.SUNDAY -> false
					else -> true
				}
			}
		}

		val singleRowCalendar = root.findViewById<SingleRowCalendar>(R.id.main_single_row_calendar).apply {
			calendarViewManager = myCalendarViewManager
			calendarChangesObserver = myCalendarChangesObserver
			calendarSelectionManager = mySelectionManager
			futureDaysCount = 30
			includeCurrentDate = true
			init()
		}

		btnRight.setOnClickListener {
			singleRowCalendar.setDates(getDatesOfNextMonth())
		}

		btnLeft.setOnClickListener {
			singleRowCalendar.setDates(getDatesOfPreviousMonth())
		}

		return root
	}

	private fun getDatesOfNextMonth(): List<Date> {
		currentMonth++ // + because we want next month
		if (currentMonth == 12) {
			// we will switch to january of next year, when we reach last month of year
			calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] + 1)
			currentMonth = 0 // 0 == january
		}
		return getDates(mutableListOf())
	}

	private fun getDatesOfPreviousMonth(): List<Date> {
		currentMonth-- // - because we want previous month
		if (currentMonth == -1) {
			// we will switch to december of previous year, when we reach first month of year
			calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
			currentMonth = 11 // 11 == december
		}
		return getDates(mutableListOf())
	}

	private fun getFutureDatesOfCurrentMonth(): List<Date> {
		// get all next dates of current month
		currentMonth = calendar[Calendar.MONTH]
		return getDates(mutableListOf())
	}


	private fun getDates(list: MutableList<Date>): List<Date> {
		// load dates of whole month
		calendar.set(Calendar.MONTH, currentMonth)
		calendar.set(Calendar.DAY_OF_MONTH, 1)
		list.add(calendar.time)
		while (currentMonth == calendar[Calendar.MONTH]) {
			calendar.add(Calendar.DATE, +1)
			if (calendar[Calendar.MONTH] == currentMonth)
				list.add(calendar.time)
		}
		calendar.add(Calendar.DATE, -1)
		return list
	}
}
