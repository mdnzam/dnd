package com.inod.dnd

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.inod.dnd.R

class DndTileService : TileService() {

    override fun onCreate() {
        super.onCreate()
        DndController.init(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        DndController.refresh()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (!DndController.state.value.permissionGranted) {
            DndController.openPolicySettings(this)
            return
        }
        DndController.toggle()
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val state = DndController.state.value

        tile.state = if (state.active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (state.active) state.typeLabel else "DND"
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_dnd)
        tile.updateTile()
    }
}
