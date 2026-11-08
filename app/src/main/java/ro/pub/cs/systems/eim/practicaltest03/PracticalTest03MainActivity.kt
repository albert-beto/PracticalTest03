package ro.pub.cs.systems.eim.practicaltest03

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PracticalTest03MainActivity : AppCompatActivity() {

    private lateinit var editTextProdus: EditText
    private lateinit var editTextCantitate: EditText
    private lateinit var radioGroupPrioritate: RadioGroup
    private lateinit var radioButtonLow: RadioButton
    private lateinit var radioButtonNormal: RadioButton
    private lateinit var radioButtonHigh: RadioButton
    private lateinit var buttonAdd: Button
    private lateinit var buttonUndo: Button
    private lateinit var buttonCheckout: Button
    private lateinit var editTextProduse: EditText

    private var produse = ArrayList<String>()
    private var total = 0
    private val buttonClickListener = ButtonClickListener()

    private inner class CountdownReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val total = intent.getIntExtra(Constants.TOTAL, 0)
            val index = intent.getIntExtra(Constants.EXTRA_INDEX, 0)

            val label = if (intent.action == Constants.ACTION_TICK) "TICK" else "TOCK"
            if (index == 5) {
                Toast.makeText(this@PracticalTest03MainActivity, "Countdown finished", Toast.LENGTH_SHORT).show()
            }
            Log.d(Constants.TAG, "${label} ${index}/5 (total ${total})")
        }
    }
    private val countdownReceiver = CountdownReceiver()
    private val intentFilter = IntentFilter()

    private val secondaryActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == RESULT_OK) {
                val total = result.data?.getIntExtra(Constants.TOTAL, 0)
                val voucher = result.data?.getStringExtra(Constants.VOUCHER)
                if (voucher == null)
                    Toast.makeText(this@PracticalTest03MainActivity, "Paid ${total} items", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@PracticalTest03MainActivity, "Paid ${total} items with code ${voucher}",
                        Toast.LENGTH_SHORT).show()
                produse = ArrayList<String>()
                recalculeazaTotal()
                editTextProduse.setText("")
            } else if (result.resultCode == RESULT_FIRST_USER)
                Toast.makeText(this@PracticalTest03MainActivity, "Back to editing", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this@PracticalTest03MainActivity, "Checkout canceled", Toast.LENGTH_SHORT).show()
            }

        }
    private lateinit var editTextTotal: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test03_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTextProdus = findViewById(R.id.EditTextProdus)
        editTextCantitate = findViewById(R.id.EditTextCantitate)
        editTextProduse = findViewById(R.id.EditTextProduse)
        editTextTotal = findViewById(R.id.EditTextTotal)
        radioGroupPrioritate = findViewById(R.id.RadioGroupPrioritate)
        radioButtonLow = findViewById(R.id.RadioButtonLow)
        radioButtonNormal = findViewById(R.id.RadioButtonNormal)
        radioButtonHigh = findViewById(R.id.RadioButtonHigh)
        buttonAdd = findViewById(R.id.ButtonAdd)
        buttonUndo = findViewById(R.id.ButtonUndo)
        buttonCheckout = findViewById(R.id.ButtonCheckout)

        radioGroupPrioritate.check(R.id.RadioButtonNormal)

        buttonAdd.setOnClickListener(buttonClickListener)
        buttonUndo.setOnClickListener(buttonClickListener)
        buttonCheckout.setOnClickListener(buttonClickListener)

        intentFilter.addAction(Constants.ACTION_TICK)
        intentFilter.addAction(Constants.ACTION_TOCK)

        ContextCompat.registerReceiver(this, countdownReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private inner class ButtonClickListener: View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ButtonAdd -> {
                    if (editTextProdus.text.isEmpty() || editTextCantitate.text.isEmpty() || editTextCantitate.text.toString().toInt() == 0)
                        Toast.makeText(this@PracticalTest03MainActivity, "Introdu produs sau cantitiate", Toast.LENGTH_SHORT).show()
                    else {
                        var message = editTextCantitate.text.toString() + " x " + editTextProdus.text.toString()
                        if (radioGroupPrioritate.checkedRadioButtonId == R.id.RadioButtonHigh) {
                            message = "! " + message
                        } else {
                            message = "  " + message
                        }
                        produse.add(message)
                        editTextProduse.setText(produse.joinToString("\n"))
                        recalculeazaTotal()
                        if (total > Constants.TOTAL_THRESHOLD) {
                            val intent = Intent(this@PracticalTest03MainActivity,
                                PracticalTest03Service::class.java)
                            intent.putExtra(Constants.TOTAL,total)
                            startService(intent)
                        }
                        editTextProdus.setText("")
                        editTextCantitate.setText("")
                        radioGroupPrioritate.check(R.id.RadioButtonNormal)
                    }
                }
                R.id.ButtonUndo -> {
                    if(produse.isEmpty())
                        Toast.makeText(this@PracticalTest03MainActivity, "Cos gol", Toast.LENGTH_SHORT).show()
                    else {
                        produse.removeAt(produse.size - 1)
                        recalculeazaTotal()
                        editTextProduse.setText(produse.joinToString("\n"))
                    }
                }
                R.id.ButtonCheckout -> {
                    if (produse.isEmpty())
                        Toast.makeText(this@PracticalTest03MainActivity, "Cos gol", Toast.LENGTH_SHORT).show()
                    else {
                        val bundle = Bundle()
                        bundle.putInt(Constants.TOTAL, total)
                        bundle.putStringArrayList(Constants.PRODUSE, produse)
                        val intent = Intent(this@PracticalTest03MainActivity,
                            PracticalTest03CheckoutActivity::class.java)
                        intent.putExtra(Constants.BUNDLE, bundle)
                        secondaryActivityLauncher.launch(intent)

                    }
                }
            }
        }
    }

    private fun recalculeazaTotal() {
        total = 0
        for (produs in produse) {
            val new = produs.substring(2)
            total += new.split(" x ")[0].toInt()
        }
        editTextTotal.setText("Total: " + total.toString() + " buc.")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey(Constants.PRODUSE)) {
            produse = savedInstanceState.getStringArrayList(Constants.PRODUSE) ?: ArrayList()
            recalculeazaTotal()
            editTextProduse.setText(produse.joinToString("\n"))
        }
        if (savedInstanceState.containsKey(Constants.PRIORITATE))
            radioGroupPrioritate.check(savedInstanceState.getInt(Constants.PRIORITATE))

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(Constants.PRODUSE, produse)
        outState.putInt(Constants.PRIORITATE, radioGroupPrioritate.checkedRadioButtonId)
    }

    override fun onDestroy() {
        val intent = Intent(this, PracticalTest03Service::class.java)
        stopService(intent)
        unregisterReceiver(countdownReceiver)
        super.onDestroy()
    }

}