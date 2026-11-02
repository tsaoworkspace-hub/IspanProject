<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import httpClient from '@/service/httpClient'

const emit = defineEmits(['storeSelected'])

const loading = ref(true)
const errorMessage = ref('')
const mapStores = ref([])

// 目前選擇的城市
const selectedCity = ref('高雄市')

// 依門市數量排序：高雄 20 → 大城市 10 → 其他縣市 1
const cities = [
  '高雄市',
  '台北市', '新北市', '桃園市', '台中市', '台南市',
  '基隆市', '新竹市', '新竹縣', '苗栗縣', '彰化縣', '南投縣',
  '雲林縣', '嘉義市', '嘉義縣', '屏東縣', '宜蘭縣', '花蓮縣',
  '台東縣', '澎湖縣', '金門縣', '連江縣'
]

// 各城市市中心座標（市政府附近）
// 沒列在這裡的縣市，地圖會以第一間門市為中心
const CITY_CENTERS = {
  '高雄市': [22.6273, 120.3014],
  '台北市': [25.0375, 121.5637],
  '新北市': [25.0120, 121.4650],
  '桃園市': [24.9936, 121.3010],
  '台中市': [24.1618, 120.6469],
  '台南市': [22.9920, 120.1850]
}

let map = null

// Leaflet 預設 marker 圖示修正
delete L.Icon.Default.prototype._getIconUrl

L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
})


// ============================================================
// 選擇門市
// ============================================================

function selectStore(store) {
  console.log('選擇的門市：', store)

  emit('storeSelected', {
    storeId: store.storeId,
    storeName: store.storeName,
    address: store.address,
    phone: store.phone,
    chain: store.chain,
    city: store.city,
    latitude: store.latitude,
    longitude: store.longitude
  })

  if (map) {
    map.closePopup()
  }
}


// ============================================================
// 取得門市資料
// ============================================================

async function loadStores() {
  try {
    loading.value = true
    errorMessage.value = ''

    console.log(
      `開始取得 ${selectedCity.value} 門市資料`
    )

    const response = await httpClient.get(
      '/logistics/cvs/stores',
      {
        params: {
          chain: 'UNIMART',
          city: selectedCity.value
        }
      }
    )

    console.log(
      `${selectedCity.value} 門市資料：`,
      response.data
    )

    // 只保留有座標的門市
    mapStores.value = response.data.filter(store => {
      return (
        store.latitude !== null &&
        store.longitude !== null &&
        !isNaN(Number(store.latitude)) &&
        !isNaN(Number(store.longitude))
      )
    })

    console.log(
      `${selectedCity.value} 共有 ${mapStores.value.length} 間門市具有座標`
    )

    // 沒有座標
    if (mapStores.value.length === 0) {
      errorMessage.value =
        `${selectedCity.value} 目前沒有可顯示的門市座標`

      return
    }

    await nextTick()

    // 第一次建立地圖，之後只更新 Marker
    initMap()

  } catch (error) {
    console.error('取得門市資料失敗：', error)

    errorMessage.value =
      error.response?.data?.message ||
      '門市資料載入失敗，請稍後再試'

  } finally {
    loading.value = false
  }
}


// ============================================================
// 切換城市
// ============================================================

function changeCity() {
  console.log(
    `切換城市：${selectedCity.value}`
  )

  loadStores()
}


// ============================================================
// 初始化 / 更新地圖
// ============================================================

function initMap() {

  const mapElement = document.getElementById('store-map')

  if (!mapElement) {
    console.error('找不到 store-map')
    errorMessage.value = '地圖元件初始化失敗'
    return
  }


  // ==========================================================
  // 第一次才建立 Leaflet 地圖
  // ==========================================================

  if (!map) {

    map = L.map(mapElement)

    // OpenStreetMap
    L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19
      }
    ).addTo(map)
  }


  // ==========================================================
  // 清除舊 Marker
  // ==========================================================

  map.eachLayer(layer => {

    if (layer instanceof L.Marker) {
      map.removeLayer(layer)
    }

  })


  // ==========================================================
  // 建立新的 Marker
  // ==========================================================

  mapStores.value.forEach(store => {

    const latitude = Number(store.latitude)
    const longitude = Number(store.longitude)

    const marker = L.marker([
      latitude,
      longitude
    ]).addTo(map)


    // Popup
    const popupContent = document.createElement('div')
    popupContent.style.minWidth = '220px'

    const title = document.createElement('strong')
    title.textContent = `7-ELEVEN ${store.storeName || ''}`

    const address = document.createElement('span')
    address.textContent = `地址：${store.address || '無資料'}`

    const phone = document.createElement('span')
    phone.textContent = `電話：${store.phone || '無資料'}`

    const button = document.createElement('button')
    button.className = 'store-select-button'
    button.textContent = '選擇此門市'
    button.style.cssText = `
      width: 100%;
      padding: 8px 12px;
      border: none;
      border-radius: 5px;
      background: #198754;
      color: white;
      cursor: pointer;
    `

    popupContent.append(
      title,
      document.createElement('br'),
      document.createElement('br'),
      address,
      document.createElement('br'),
      phone,
      document.createElement('br'),
      document.createElement('br'),
      button,
    )


    button.addEventListener(
      'click',
      () => {
        selectStore(store)
      }
    )


    marker.bindPopup(
      popupContent
    )

  })


  // ==========================================================
  // 確保地圖尺寸正確後，置中在市中心
  // （沒有市中心座標的縣市，改以第一間門市為中心）
  // ==========================================================

  setTimeout(() => {

    if (map) {
      map.invalidateSize()

      const firstStore = mapStores.value[0]
      const center =
        CITY_CENTERS[selectedCity.value] ||
        [Number(firstStore.latitude), Number(firstStore.longitude)]

      map.setView(center, 12)
    }

  }, 100)
}


// ============================================================
// 元件載入
// ============================================================

onMounted(() => {
  loadStores()
})


// ============================================================
// 元件離開
// ============================================================

onBeforeUnmount(() => {

  if (map) {

    map.remove()

    map = null
  }

})
</script>
<template>

  <div>

    <!-- ======================================================
         城市選擇
    ======================================================= -->

    <div class="mb-3">

      <label
        for="citySelect"
        class="form-label fw-semibold"
      >
        選擇取貨城市
      </label>


      <select
        id="citySelect"
        v-model="selectedCity"
        class="form-select"
        @change="changeCity"
      >

        <option
          v-for="city in cities"
          :key="city"
          :value="city"
        >
          {{ city }}
        </option>

      </select>

    </div>


    <!-- ======================================================
         載入中
    ======================================================= -->

    <div
      v-if="loading"
      class="border rounded p-4 text-center text-secondary"
    >

      <div class="mb-2">
        門市資料載入中...
      </div>

      <small>
        正在取得
        {{ selectedCity }}
        7-ELEVEN 門市資料
      </small>

    </div>


    <!-- ======================================================
         錯誤
    ======================================================= -->

    <div
      v-else-if="errorMessage"
      class="alert alert-danger"
    >

      {{ errorMessage }}

    </div>


    <!-- ======================================================
         Leaflet 地圖
    ======================================================= -->

    <div
      v-show="!loading && !errorMessage"
      id="store-map"
      style="
        width: 100%;
        height: 400px;
        border-radius: 8px;
        overflow: hidden;
      "
    ></div>



  </div>

</template>
