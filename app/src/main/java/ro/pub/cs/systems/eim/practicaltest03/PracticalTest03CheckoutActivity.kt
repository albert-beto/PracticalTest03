package ro.pub.cs.systems.eim.practicaltest03

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PracticalTest03CheckoutActivity : AppCompatActivity() {
    private lateinit var editTextVoucher: EditText
    private lateinit var buttonPay: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonCancel: Button
    private lateinit var editTextLista: EditText
    private lateinit var editTextTotalul: EditText

    private var produse = ArrayList<String>()
    private var total = 0

    private val buttonClickListener = ButtonClickListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test03_checkout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextVoucher = findViewById(R.id.EditTextVoucher)
        buttonPay = findViewById(R.id.ButtonPay)
        buttonEdit = findViewById(R.id.ButtonEdit)
        buttonCancel = findViewById(R.id.ButtonCancel)
        editTextLista = findViewById(R.id.EditTextLista)
        editTextTotalul = findViewById(R.id.EditTextTotalul)

        val bundle = intent.getBundleExtra(Constants.BUNDLE)
        produse = bundle?.getStringArrayList(Constants.PRODUSE) ?: ArrayList()
        total = bundle?.getInt(Constants.TOTAL) ?: 0
        editTextTotalul.setText("Total: " + total.toString() + " buc.")
        editTextLista.setText(produse.joinToString("\n"))

        buttonPay.setOnClickListener(buttonClickListener)
        buttonEdit.setOnClickListener(buttonClickListener)
        buttonCancel.setOnClickListener(buttonClickListener)

    }

    private inner class ButtonClickListener: View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ButtonPay -> {
                    val intent = Intent()
                    intent.putExtra(Constants.TOTAL, total)
                    if (editTextVoucher.text.isEmpty())
                        setResult(RESULT_OK, intent)
                    else {
                        intent.putExtra(Constants.VOUCHER, editTextVoucher.text.toString())
                        setResult(RESULT_OK, intent)
                    }

                }
                R.id.ButtonEdit -> {
                    setResult(RESULT_FIRST_USER)
                }
                R.id.ButtonCancel -> {
                    setResult(RESULT_CANCELED)
                }
            }
            finish()
        }
    }
}