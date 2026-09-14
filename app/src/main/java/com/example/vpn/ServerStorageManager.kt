package com.example.vpn

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ServerStorageManager {
    private const val PREFS_NAME = "vpn_servers_prefs"
    private const val KEY_SERVERS = "saved_servers_json"
    private const val ADMIN_PIN = "mooh2026"

    fun verifyAdminPin(enteredPin: String): Boolean {
        return enteredPin.trim() == ADMIN_PIN
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadServers(context: Context): List<VpnServer> {
        val prefs = getPrefs(context)
        val jsonString = prefs.getString(KEY_SERVERS, null)
        if (jsonString.isNullOrBlank()) {
            return VpnServer.DEFAULT_SERVERS
        }

        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<VpnServer>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(serverFromJson(obj))
            }
            if (list.isEmpty()) VpnServer.DEFAULT_SERVERS else list
        } catch (e: Exception) {
            VpnServer.DEFAULT_SERVERS
        }
    }

    fun saveServers(context: Context, servers: List<VpnServer>) {
        val prefs = getPrefs(context)
        val jsonArray = JSONArray()
        for (server in servers) {
            jsonArray.put(serverToJson(server))
        }
        prefs.edit().putString(KEY_SERVERS, jsonArray.toString()).apply()
    }

    fun resetToDefaults(context: Context): List<VpnServer> {
        saveServers(context, VpnServer.DEFAULT_SERVERS)
        return VpnServer.DEFAULT_SERVERS
    }

    private fun serverToJson(server: VpnServer): JSONObject {
        return JSONObject().apply {
            put("id", server.id)
            put("countryName", server.countryName)
            put("countryNameAr", server.countryNameAr)
            put("city", server.city)
            put("cityAr", server.cityAr)
            put("flagEmoji", server.flagEmoji)
            put("host", server.host)
            put("port", server.port)
            put("dnsServer", server.dnsServer)
            put("secondaryDns", server.secondaryDns)
            put("pingMs", server.pingMs)
            put("loadPercent", server.loadPercent)
            put("protocol", server.protocol)
            put("isCustom", server.isCustom)
            put("rawConfig", server.rawConfig ?: "")
            put("username", server.username)
            put("password", server.password)
            put("category", server.category)
            put("sniHost", server.sniHost)
            put("payload", server.payload)
            put("proxyHost", server.proxyHost)
            put("proxyPort", server.proxyPort)
            put("notes", server.notes)
        }
    }

    private fun serverFromJson(obj: JSONObject): VpnServer {
        return VpnServer(
            id = obj.optString("id", "srv_${System.currentTimeMillis()}"),
            countryName = obj.optString("countryName", "Google Cloud VPS"),
            countryNameAr = obj.optString("countryNameAr", "سيرفر Google Cloud"),
            city = obj.optString("city", "VPS Node"),
            cityAr = obj.optString("cityAr", "سيرفر قوقل كلاود"),
            flagEmoji = obj.optString("flagEmoji", "🌐"),
            host = obj.optString("host", "1.1.1.1"),
            port = obj.optInt("port", 443),
            dnsServer = obj.optString("dnsServer", "8.8.8.8"),
            secondaryDns = obj.optString("secondaryDns", "1.1.1.1"),
            pingMs = obj.optInt("pingMs", -1),
            loadPercent = obj.optInt("loadPercent", 25),
            protocol = obj.optString("protocol", "SSH"),
            isCustom = obj.optBoolean("isCustom", true),
            rawConfig = obj.optString("rawConfig", null).takeIf { !it.isNullOrBlank() },
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
            category = obj.optString("category", "GCP"),
            sniHost = obj.optString("sniHost", ""),
            payload = obj.optString("payload", ""),
            proxyHost = obj.optString("proxyHost", ""),
            proxyPort = obj.optInt("proxyPort", 0),
            notes = obj.optString("notes", "")
        )
    }
}
