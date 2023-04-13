package com.example.die
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var dieRadioGroup: RadioGroup
    private lateinit var customDieEditText: EditText
    private lateinit var rollButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to UI components
        dieRadioGroup = findViewById(R.id.die_radio_group)
        customDieEditText = findViewById(R.id.custom_die_edit_text)
        rollButton = findViewById(R.id.roll_button)
        resultTextView = findViewById(R.id.result_text_view)

        // Get shared preferences
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // Set up spinner to select number of dice to roll
        val diceNumbersSpinner = findViewById<Spinner>(R.id.custom_die_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.dice_numbers_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            diceNumbersSpinner.adapter = adapter
        }

        // Set up ListView to display previous rolls
        val previousRollsListView = findViewById<ListView>(R.id.previous_rolls_list_view)
        val previousRolls = mutableListOf<String>()
        val previousRollsAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, previousRolls)
        previousRollsListView.adapter = previousRollsAdapter

        // Set up "Roll 2 Dice" checkbox and switch
        val roll2DiceCheckbox = findViewById<CheckBox>(R.id.roll_two_dice_checkbox)
        val rollInTensSwitch = findViewById<Switch>(R.id.roll_in_tens_switch)

        // Set up click listener for roll button
        rollButton.setOnClickListener {
            val selectedDie = when (dieRadioGroup.checkedRadioButtonId) {
                R.id.four_sided_die_radio_button -> 4
                R.id.six_sided_die_radio_button -> 6
                R.id.eight_sided_die_radio_button -> 8
                R.id.ten_sided_die_radio_button -> if (rollInTensSwitch.isChecked) 10 else 9
                R.id.twelve_sided_die_radio_button -> 12
                R.id.twenty_sided_die_radio_button -> 20
                else -> customDieEditText.text.toString().toIntOrNull() ?: return@setOnClickListener
            }

            val diceCount = diceNumbersSpinner.selectedItem.toString().toIntOrNull()
                ?: return@setOnClickListener

            val rolls = mutableListOf<Int>()
            repeat(if (roll2DiceCheckbox.isChecked) diceCount * 2 else diceCount) {
                rolls.add(Random.nextInt(1, selectedDie + 1))
            }

            val resultText = if (roll2DiceCheckbox.isChecked) {
                "Dice 1: ${rolls[0]}, Dice 2: ${rolls[1]}"
            } else {
                rolls.joinToString(", ")
            }
            resultTextView.text = resultText

            // Add roll to previous rolls and save to shared preferences
            val previousRoll = "$selectedDie-sided die, $diceCount rolls: $resultText"
            previousRolls.add(0, previousRoll)
            while (previousRolls.size > 10) {
                previousRolls.removeAt(previousRolls.lastIndex)
            }
            previousRollsAdapter.notifyDataSetChanged()
            sharedPreferences.edit().putStringSet("previous_rolls.Set", previousRolls.toSet())
                .apply()
        }

        // Load previous rolls from shared preferences
        val savedRolls = sharedPreferences.getStringSet("previous_rolls_set", setOf())
        previousRolls.addAll(savedRolls ?: emptySet())
        previousRollsAdapter.notifyDataSetChanged()
    }
}
