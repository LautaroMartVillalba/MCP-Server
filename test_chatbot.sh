#!/bin/bash

# Script para probar las capacidades del chatbot del sistema de reservas hoteleras
# Realiza 10 consultas realistas simulando usuarios reales
# Guarda los logs de cada respuesta

ENDPOINT="http://localhost:8080/chat"
LOG_DIR="./chatbot_test_logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Crear directorio de logs si no existe
mkdir -p "$LOG_DIR"

echo "=========================================="
echo "Iniciando pruebas del chatbot hotelero"
echo "Timestamp: $TIMESTAMP"
echo "=========================================="
echo ""

# Función para realizar una consulta
make_query() {
    local query_num=$1
    local prompt=$2
    local log_file="$LOG_DIR/query_${query_num}_${TIMESTAMP}.json"
    
    echo "[$query_num/10] Consultando: \"$prompt\""
    echo "Guardando respuesta en: $log_file"
    
    # URL encode el prompt y realizar el curl
    local encoded_prompt=$(printf %s "$prompt" | jq -sRr @uri)
    
    # Realizar el curl y guardar la respuesta
    curl -s -X GET "${ENDPOINT}?prompt=${encoded_prompt}" \
        -o "$log_file" \
        -w "\nCódigo HTTP: %{http_code}\nTiempo total: %{time_total}s\n"
    
    echo "Respuesta guardada. Esperando 20 segundos..."
    echo "----------------------------------------"
    echo ""
    sleep 20
}

# ========================================
# CONSULTA 1: Búsqueda básica de hoteles con características específicas
# ========================================
make_query 1 "Hola, estoy buscando hoteles de 4 o 5 estrellas que tengan spa y piscina. ¿Qué opciones hay disponibles?"

# ========================================
# CONSULTA 2: Búsqueda por ubicación y precio
# ========================================
make_query 2 "Necesito un hotel en Buenos Aires con habitaciones que cuesten entre 150 y 250 dólares por noche. ¿Qué me recomiendas?"

# ========================================
# CONSULTA 3: Búsqueda por tipo de habitación específico
# ========================================
make_query 3 "Quiero reservar una suite presidencial para mi aniversario el próximo mes. ¿Cuáles son mis opciones?"

# ========================================
# CONSULTA 4: Consulta sobre disponibilidad y fechas
# ========================================
make_query 4 "¿Hay hoteles con habitaciones disponibles para 4 personas entre el 15 de enero y el 20 de enero de 2026?"

# ========================================
# CONSULTA 5: Búsqueda por beneficios específicos
# ========================================
make_query 5 "Estoy buscando un hotel que incluya desayuno gratuito y wifi. ¿Tienes información sobre eso?"

# ========================================
# CONSULTA 6: Consulta de información específica de un hotel
# ========================================
make_query 6 "¿Puedes darme más detalles sobre el Hotel Grand Plaza? Me interesa saber qué atracciones y beneficios ofrece."

# ========================================
# CONSULTA 7: Búsqueda por múltiples criterios combinados
# ========================================
make_query 7 "Busco un hotel de 5 estrellas con habitaciones deluxe, que tenga gimnasio, restaurante y que cueste menos de 300 dólares la noche."

# ========================================
# CONSULTA 8: Consulta sobre reservas existentes
# ========================================
make_query 8 "¿Puedes mostrarme todas las reservas activas que tengo? Mi ID de cliente es 1."

# ========================================
# CONSULTA 9: Búsqueda específica por capacidad y comodidades
# ========================================
make_query 9 "Necesito un hotel familiar con habitaciones para 3 personas, que tenga estacionamiento gratuito y esté cerca de atracciones turísticas."

# ========================================
# CONSULTA 10: Consulta compleja de planificación
# ========================================
make_query 10 "Quiero organizar un viaje de negocios para 5 personas en marzo de 2026. Necesitamos un hotel de al menos 4 estrellas con sala de conferencias, buen wifi y habitaciones ejecutivas. ¿Qué opciones tenemos?"

echo "=========================================="
echo "Pruebas completadas"
echo "Logs guardados en: $LOG_DIR"
echo "Total de consultas: 10"
echo "=========================================="

# Generar resumen
echo ""
echo "Resumen de archivos generados:"
ls -lh "$LOG_DIR" | grep "${TIMESTAMP}"
