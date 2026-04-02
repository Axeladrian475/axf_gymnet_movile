package com.example.axf_movile.network

import com.example.axf_movile.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz Retrofit con todos los endpoints del backend AXF GymNet
 * que usa la aplicación móvil del suscriptor.
 */
interface ApiService {

    // ─── AUTH ────────────────────────────────────────────────────────────────
    // NUEVO endpoint — debe agregarse al backend en auth.routes.js
    @POST("auth/login-suscriptor")
    suspend fun loginSuscriptor(@Body request: LoginRequest): Response<LoginResponse>

    // ─── SUSCRIPTOR ──────────────────────────────────────────────────────────
    @GET("suscriptores/{id}")
    suspend fun getSuscriptor(@Path("id") id: Int): Response<Suscriptor>

    @GET("suscriptores/{id}/suscripcion-activa")
    suspend fun getSuscripcionActiva(@Path("id") id: Int): Response<SuscripcionActiva>

    // ─── RUTINAS (ENTRENAMIENTO) ──────────────────────────────────────────────
    // NUEVO endpoint — debe agregarse en entrenamiento.routes.js
    @GET("entrenamiento/rutinas/suscriptor/{id}")
    suspend fun getRutinasSuscriptor(@Path("id") id: Int): Response<List<Rutina>>

    @GET("entrenamiento/rutinas/{id}")
    suspend fun getRutinaDetalle(@Path("id") id: Int): Response<Rutina>

    // ─── DIETAS (NUTRICIÓN) ───────────────────────────────────────────────────
    @GET("nutricion/dietas/{id_suscriptor}")
    suspend fun getDietasSuscriptor(@Path("id_suscriptor") id: Int): Response<List<Dieta>>

    @GET("nutricion/registros/{id_suscriptor}")
    suspend fun getRegistrosFisicos(@Path("id_suscriptor") id: Int): Response<List<RegistroFisico>>

    // ─── REPORTES ─────────────────────────────────────────────────────────────
    // NUEVO endpoint — debe agregarse en incidencias.routes.js
    @GET("incidencias/publicos/{id_sucursal}")
    suspend fun getReportesPublicos(@Path("id_sucursal") idSucursal: Int): Response<List<Reporte>>

    @GET("incidencias/mis-reportes")
    suspend fun getMisReportes(): Response<List<Reporte>>

    @Multipart
    @POST("incidencias")
    suspend fun crearReporte(
        @Part("id_sucursal") idSucursal: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("es_privado") esPrivado: RequestBody,
        @Part("id_personal_reportado") idPersonalReportado: RequestBody?,
        @Part("sobre_atencion_previa") sobreAtencionPrevia: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<MessageResponse>

    // NUEVO endpoint — sumarse a reporte
    @POST("incidencias/{id}/sumarse")
    suspend fun sumarseReporte(@Path("id") idReporte: Int): Response<MessageResponse>

    // ─── CHAT ─────────────────────────────────────────────────────────────────
    // NUEVO endpoint — debe agregarse en el backend
    @GET("chat/conversaciones")
    suspend fun getConversaciones(): Response<List<ChatConversacion>>

    @GET("chat/mensajes/{id_personal}")
    suspend fun getMensajes(@Path("id_personal") idPersonal: Int): Response<List<ChatMensaje>>

    @POST("chat/mensaje")
    suspend fun enviarMensaje(@Body request: ChatMensajeRequest): Response<MessageResponse>

    // ─── PERSONAL ─────────────────────────────────────────────────────────────
    // NUEVO endpoint — personal de una sucursal para reportes
    @GET("personal/sucursal/{id_sucursal}/publico")
    suspend fun getPersonalSucursal(@Path("id_sucursal") idSucursal: Int): Response<List<Personal>>

    // ─── SUCURSALES ───────────────────────────────────────────────────────────
    @GET("sucursales/publico")
    suspend fun getSucursales(): Response<List<Sucursal>>

    // ─── RECOMPENSAS ──────────────────────────────────────────────────────────
    @GET("recompensas")
    suspend fun getRecompensas(): Response<List<Recompensa>>
}
