package it.achdjian.plugin.esp32.entry_type

import it.achdjian.plugin.espparser.SimpleExpression

open class IntConfigEntry(
    text: String,
    configEntry: String,
    description: String,
    values: List<Value>,
    private val minStr: String,
    private val maxStr: String
) : SdkConfigEntry(text, description, configEntry, values) {

    val min: Long get() = convertoToLong(minStr)
    val max: Long get() = convertoToLong(maxStr)

    private val listeners = ArrayList<(value: Int) -> Unit>()

    var value: Int
        get() {
            values.forEach {
                if (eval(it.condition)) {
                    return eval(it.value)
                }
            }
            return 0
        }
        set(newVal) {
            if (values.size == 1) {
                values = listOf(Value(SimpleExpression(newVal.toString())))
                listeners.forEach { it(newVal) }
            }
        }


    override fun set(key: String, newValue: String) {
        if (key == configEntry)
            value = newValue.toInt()
    }

    override fun addConfiguration(configurations: MutableList<Pair<String, String>>) {
        if (enabled) {
            configurations.add(Pair(configEntry, value.toString()))
        }
    }


    private fun convertoToLong(value: String): Long {
        return ConfigElements.configElements[value]?.let {
            if (it is IntConfigEntry) {
                0L
            } else {
                return 0L
            }
        } ?: if (value.startsWith("0x"))
            value.substring(2).toLong(16);
        else
            value.toLong()

    }

}

class HexConfigEntry(
    text: String,
    configEntry: String,
    description: String,
    values: List<Value>,
    min: String,
    max: String
) : IntConfigEntry(text, configEntry, description, values, min, max) {

    override fun addConfiguration(configurations: MutableList<Pair<String, String>>) {
        if (enabled) {
            configurations.add(Pair(configEntry, "0x" + value.toString(16)))
        }
    }

    override fun set(key: String, newValue: String) {
        if (configEntry == key) {
            try {

                if (newValue.startsWith("0x")) {
                    value = newValue.substring(2).toInt(16)
                } else {
                    value = newValue.toInt(16)
                }
            } catch (e: Exception) {
                throw RuntimeException("Unable to convert the $key with value $newValue to an Integer", e)
            }
        }
    }
}


