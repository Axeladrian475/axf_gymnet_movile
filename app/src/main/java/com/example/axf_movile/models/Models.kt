package com.example.axf_movile.models

import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: SuscriptorSession
)

data class SuscriptorSession(
    @SerializedName("id_suscriptor") val idSuscriptor: Int,
    val nombre: String,
    val correo: String,
    @SerializedName("id_sucursal_registro") val idSucursalRegistro: Int,
    val puntos: Int,
    @SerializedName("racha_dias") val rachaDias: Int,
    val activo: Int
)

// ─── Suscripción ─────────────────────────────────────────────────────────────

data class SuscripcionActiva(
    @SerializedName("id_suscripcion") val idSuscripcion: Int,
    @SerializedName("nombre_tipo") val nombreTipo: String,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val estado: String,
    @SerializedName("sesiones_entrenador_restantes") val sesionesEntrenador: Int,
    @SerializedName("sesiones_nutriologo_restantes") val sesionesNutriologo: Int
)

// ─── Suscriptor completo ──────────────────────────────────────────────────────

data class Suscriptor(
    @SerializedName("id_suscriptor") val idSuscriptor: Int,
    val nombres: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String?,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String,
    val sexo: String,
    val correo: String,
    val telefono: String?,
    val puntos: Int,
    @SerializedName("racha_dias") val rachaDias: Int,
    val activo: Int
)

// ─── Entrenamiento ───────────────────────────────────────────────────────────

data class Rutina(
    @SerializedName("id_rutina") val idRutina: Int,
    @SerializedName("id_suscriptor") val idSuscriptor: Int,
    @SerializedName("nombre_entrenador") val nombreEntrenador: String?,
    @SerializedName("notas_pdf") val notasPdf: String?,
    @SerializedName("creado_en") val creadoEn: String,
    val ejercicios: List<RutinaEjercicio>?
)

data class RutinaEjercicio(
    val id: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("nombre_ejercicio") val nombreEjercicio: String,
    @SerializedName("imagen_url") val imagenUrl: String?,
    val orden: Int,
    val series: Int,
    val repeticiones: Int,
    @SerializedName("descanso_seg") val descansoSeg: Int?,
    @SerializedName("peso_kg") val pesoKg: Double?,
    @SerializedName("descripcion_tecnica") val descripcionTecnica: String?
)

// ─── Nutrición ───────────────────────────────────────────────────────────────

data class Dieta(
    @SerializedName("id_dieta") val idDieta: Int,
    @SerializedName("nombre_nutriologo") val nombreNutriologo: String?,
    @SerializedName("creado_en") val creadoEn: String,
    val comidas: List<DietaComida>?
)

data class DietaComida(
    @SerializedName("id_comida") val idComida: Int,
    @SerializedName("nombre_comida") val nombreComida: String,
    @SerializedName("tiempo_comida") val tiempoComida: String,
    val calorias: Int?,
    val proteinas: Double?,
    val carbohidratos: Double?,
    val grasas: Double?,
    val notas: String?,
    val ingredientes: List<String>?
)

data class RegistroFisico(
    @SerializedName("id_registro") val idRegistro: Int,
    val peso: Double?,
    val altura: Double?,
    @SerializedName("porcentaje_grasa") val porcentajeGrasa: Double?,
    @SerializedName("porcentaje_musculo") val porcentajeMusculo: Double?,
    @SerializedName("nivel_actividad") val nivelActividad: String?,
    val objetivo: String?,
    val tmb: Double?,
    @SerializedName("gasto_energetico") val gastoEnergetico: Double?,
    val notas: String?,
    @SerializedName("nombre_nutriologo") val nombreNutriologo: String?,
    @SerializedName("creado_en") val creadoEn: String
)

// ─── Reportes ─────────────────────────────────────────────────────────────────

data class ReporteRequest(
    @SerializedName("id_sucursal") val idSucursal: Int,
    val categoria: String,
    val descripcion: String,
    @SerializedName("es_privado") val esPrivado: Boolean,
    @SerializedName("id_personal_reportado") val idPersonalReportado: Int?,
    @SerializedName("sobre_atencion_previa") val sobreAtencionPrevia: Boolean?
)

data class Reporte(
    @SerializedName("id_reporte") val idReporte: Int,
    @SerializedName("id_suscriptor") val idSuscriptor: Int,
    @SerializedName("nombre_suscriptor") val nombreSuscriptor: String?,
    @SerializedName("id_sucursal") val idSucursal: Int,
    @SerializedName("nombre_sucursal") val nombreSucursal: String?,
    val categoria: String,
    val descripcion: String,
    @SerializedName("foto_url") val fotoUrl: String?,
    @SerializedName("es_privado") val esPrivado: Int,
    val estado: String,
    @SerializedName("num_strikes") val numStrikes: Int,
    @SerializedName("num_sumados") val numSumados: Int?,
    @SerializedName("ya_sumado") val yaSumado: Boolean?,
    @SerializedName("creado_en") val creadoEn: String
)

// ─── Chat ─────────────────────────────────────────────────────────────────────

data class ChatConversacion(
    @SerializedName("id_personal") val idPersonal: Int,
    @SerializedName("nombre_personal") val nombrePersonal: String,
    val puesto: String,
    @SerializedName("foto_url") val fotoUrl: String?,
    @SerializedName("ultimo_mensaje") val ultimoMensaje: String?,
    @SerializedName("ultimo_mensaje_en") val ultimoMensajeEn: String?,
    @SerializedName("no_leidos") val noLeidos: Int
)

data class ChatMensaje(
    @SerializedName("id_mensaje") val idMensaje: Int,
    @SerializedName("enviado_por") val enviadoPor: String,  // "personal" o "suscriptor"
    val contenido: String,
    val leido: Int,
    @SerializedName("enviado_en") val enviadoEn: String
)

data class ChatMensajeRequest(
    @SerializedName("id_personal") val idPersonal: Int,
    val contenido: String
)

// ─── Personal ─────────────────────────────────────────────────────────────────

data class Personal(
    @SerializedName("id_personal") val idPersonal: Int,
    val nombres: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    val puesto: String,
    @SerializedName("foto_url") val fotoUrl: String?
)

// ─── Sucursales ───────────────────────────────────────────────────────────────

data class Sucursal(
    @SerializedName("id_sucursal") val idSucursal: Int,
    val nombre: String,
    val direccion: String?,
    @SerializedName("codigo_postal") val codigoPostal: String?
)

// ─── Recompensas ──────────────────────────────────────────────────────────────

data class Recompensa(
    @SerializedName("id_recompensa") val idRecompensa: Int,
    val nombre: String,
    @SerializedName("costo_puntos") val costoPuntos: Int,
    val activa: Int
)

// ─── Respuestas genéricas ─────────────────────────────────────────────────────

data class MessageResponse(val message: String)

data class ApiError(val message: String)
