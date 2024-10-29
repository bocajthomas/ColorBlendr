package com.drdisagree.colorblendr.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.DARKER_LAUNCHER_ICONS
import com.drdisagree.colorblendr.common.Const.MODE_SPECIFIC_THEMES
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_SECONDARY_COLOR
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.common.Const.MONET_TERTIARY_COLOR
import com.drdisagree.colorblendr.common.Const.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.common.Const.SEMI_TRANSPARENT_LAUNCHER_ICONS
import com.drdisagree.colorblendr.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.selectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.putBoolean
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.databinding.FragmentSettingsAdvancedBinding
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.SystemUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView

class SettingsAdvancedFragment : Fragment() {

    private lateinit var binding: FragmentSettingsAdvancedBinding
    private val notShizukuMode: Boolean = workingMethod != Const.WorkMethod.SHIZUKU
    private val hasPixelLauncher: Boolean = SystemUtil.isAppInstalled(PIXEL_LAUNCHER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsAdvancedBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.advanced_settings, true, binding.header.toolbar)

        // Secondary color
        var monetSecondaryColor = getInt(MONET_SECONDARY_COLOR, Color.WHITE)
        binding.secondaryColorPicker.isEnabled =
            getBoolean(MONET_SEED_COLOR_ENABLED, false) && notShizukuMode
        binding.secondaryColorPicker.previewColor =
            getInt(MONET_SECONDARY_COLOR, monetSecondaryColor)
        binding.secondaryColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(10f)
                .withColor(monetSecondaryColor)
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView::class.java)
                .withListener { _: ColorPickerDialog?, color: Int ->
                    if (monetSecondaryColor != color) {
                        monetSecondaryColor = color
                        binding.secondaryColorPicker.previewColor = color
                        putInt(MONET_SECONDARY_COLOR, monetSecondaryColor)

                        applyFabricatedColors()
                    }
                }
                .show(getChildFragmentManager(), "secondaryColorPicker")
        }

        // Tertiary color
        var monetTertiaryColor = getInt(MONET_TERTIARY_COLOR, Color.WHITE)
        binding.tertiaryColorPicker.isEnabled =
            getBoolean(MONET_SEED_COLOR_ENABLED, false) && notShizukuMode
        binding.tertiaryColorPicker.previewColor = monetTertiaryColor
        binding.tertiaryColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(10f)
                .withColor(monetTertiaryColor)
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView::class.java)
                .withListener { _: ColorPickerDialog?, color: Int ->
                    if (monetTertiaryColor != color) {
                        monetTertiaryColor = color
                        binding.tertiaryColorPicker.previewColor = color
                        putInt(MONET_TERTIARY_COLOR, monetTertiaryColor)

                        applyFabricatedColors()
                    }
                }
                .show(getChildFragmentManager(), "tertiaryColorPicker")
        }

        // Mode specific themes
        binding.modeSpecificThemes.setEnabled(notShizukuMode)
        binding.modeSpecificThemes.isSwitchChecked = getBoolean(MODE_SPECIFIC_THEMES, false)
        binding.modeSpecificThemes.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(MODE_SPECIFIC_THEMES, isChecked)
            applyFabricatedColors()
        }

        // Darker launcher icons
        binding.darkerLauncherIcons.setEnabled(notShizukuMode)
        binding.darkerLauncherIcons.visibility = if (hasPixelLauncher) View.VISIBLE else View.GONE
        binding.darkerLauncherIcons.isSwitchChecked = getBoolean(DARKER_LAUNCHER_ICONS, false)
        binding.darkerLauncherIcons.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                savePixelLauncherInPerAppTheme()
            }
            putBoolean(DARKER_LAUNCHER_ICONS, isChecked)
            applyFabricatedColors()
        }

        // Semi-transparent launcher icons
        binding.semitransparentLauncher.setEnabled(notShizukuMode)
        binding.semitransparentLauncher.visibility =
            if (hasPixelLauncher) View.VISIBLE else View.GONE
        binding.semitransparentLauncher.isSwitchChecked =
            getBoolean(SEMI_TRANSPARENT_LAUNCHER_ICONS, false)
        binding.semitransparentLauncher.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                savePixelLauncherInPerAppTheme()
            }
            putBoolean(SEMI_TRANSPARENT_LAUNCHER_ICONS, isChecked)
            applyFabricatedColors()
        }

        return binding.getRoot()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            getParentFragmentManager().popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePixelLauncherInPerAppTheme() {
        if (!hasPixelLauncher || !notShizukuMode) {
            return
        }

        val selectedApps = selectedFabricatedApps
        selectedApps[PIXEL_LAUNCHER] = true
        saveSelectedFabricatedApps(selectedApps)
    }

    private fun applyFabricatedColors() {
        CoroutineScope(Dispatchers.Main).launch {
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            delay(300)
            withContext(Dispatchers.IO) {
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }
        }
    }

    companion object {
        private val TAG: String = SettingsAdvancedFragment::class.java.simpleName
    }
}