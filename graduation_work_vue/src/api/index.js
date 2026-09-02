import request from '../utils/request'

// 用户相关
const USER_URLS = {
  ADD_USER: '/user/addUser',
  DELETE_USER: '/user/deleteUser',
  UPDATE_USER: '/user/updateUser',
  GET_USERS_BY_PAGE: '/user/getUsersByPage',
  SEARCH_USERS_BY_PAGE: '/user/searchUsersByPage',
  SEARCH_USERS_BY_NAME_AND_PHONE: '/user/searchUserByNameAndPhone',
  GET_NAME: '/user/getName',
}

// 地块相关
const LAND_URLS = {
  ADD_LAND: '/land/addLand',
  DELETE_LAND: '/land/deleteLand',
  UPDATE_LAND: '/land/updateLand',
  GET_LANDS_BY_PAGE: '/land/getLandsByPage',
  SEARCH_LANDS_BY_PAGE: '/land/searchLandsByPage',
  GET_LAND_BY_USER_ID: '/land/getLandByUserId',
}

// 地块分配相关
const LAND_ALLOCATION_URLS = {
  ASSIGN_LAND: '/landAllocation/add',
  GET_CONTRACTOR_INFO: '/landAllocation/getContractorInfoByLandId',
  UPDATE_LAND_CONTRACTOR_INFO: '/landAllocation/updateContractor',
  GET_LAND_ALLOCATIONS: '/landAllocation/getLandAllocationByPage',
  SEARCH_LAND_ALLOCATIONS: '/landAllocation/searchLandAllocationInfoByPage',
  DELETE_LAND_ALLOCATION: '/landAllocation/delete',
  GET_MY_LANDS: '/landAllocation/getMyLands',
}

// 农作物相关
const CROP_URLS = {
  ADD_CROP: '/crop/addCrop',
  DELETE_CROP: '/crop/deleteCrop',
  UPDATE_CROP: '/crop/updateCrop',
  GET_CROPS_BY_PAGE: '/crop/getCropsByPage',
  SEARCH_CROPS_BY_PAGE: '/crop/searchCropsByPage',
}

const api = {
  // 登录相关
  login(data) {
    return request({ url: '/auth/login', method: 'post', data })
  },

  // 登出相关
  logout() {
    return request({ url: '/auth/logout', method: 'post' })
  },
  // 用户相关
  addUser(data) {
    return request({ url: USER_URLS.ADD_USER, method: 'post', data })
  },
  deleteUser(id) {
    return request({ url: `${USER_URLS.DELETE_USER}/${id}`, method: 'delete' })
  },
  updateUser(data) {
    return request({ url: USER_URLS.UPDATE_USER, method: 'post', data })
  },
  updatePassword(data) {
    return request({ url: '/user/updatePassword', method: 'put', data })
  },
  getUserInfo() {
    return request({ url: '/user/getUserInfo', method: 'get' })
  },
  getName() {
    return request({ url: USER_URLS.GET_NAME, method: 'get' })
  },
  getUsersByPage(pageNum, pageSize) {
    return request({ url: `${USER_URLS.GET_USERS_BY_PAGE}/${pageNum}/${pageSize}`, method: 'get' })
  },
  searchUsersByPage(keyword, pageNum, pageSize) {
    return request({
      url: `${USER_URLS.SEARCH_USERS_BY_PAGE}/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  updateUserStatus(userId, status) {
    return request({
      url: `/user/updateUserStatus/${userId}/${status}`,
      method: 'put',
    })
  },
  // 承包人相关
  getContractorsByPage(pageNum, pageSize) {
    return request({
      url: `/user/getContractorsByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  searchContractorsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/user/searchContractorsByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },

  //通过姓名和手机号查找承包人
  searchUsersByNameAndPhone(name, phone) {
    return request({
      url: `${USER_URLS.SEARCH_USERS_BY_NAME_AND_PHONE}/${name}/${phone}`,
      method: 'get',
    })
  },

  // 地块相关
  addLand(data) {
    return request({ url: LAND_URLS.ADD_LAND, method: 'post', data })
  },
  getAllLands() {
    return request({
      url: '/land/getAll',
      method: 'get',
    })
  },
  deleteLand(id) {
    return request({ url: `${LAND_URLS.DELETE_LAND}/${id}`, method: 'delete' })
  },
  updateLand(data) {
    return request({ url: LAND_URLS.UPDATE_LAND, method: 'put', data })
  },
  getLandsByPage(pageNum, pageSize) {
    return request({ url: `${LAND_URLS.GET_LANDS_BY_PAGE}/${pageNum}/${pageSize}`, method: 'get' })
  },
  searchLandsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `${LAND_URLS.SEARCH_LANDS_BY_PAGE}/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getMyLands() {
    return request({
      url: LAND_ALLOCATION_URLS.GET_MY_LANDS,
      method: 'get',
    })
  },

  // 地块分配相关
  assignLand(data) {
    return request({ url: LAND_ALLOCATION_URLS.ASSIGN_LAND, method: 'post', data })
  },
  getContractorInfoByLandId(landId) {
    return request({
      url: `${LAND_ALLOCATION_URLS.GET_CONTRACTOR_INFO}/${landId}`,
      method: 'get',
    })
  },
  deleteLandAllocation(landAllocationId) {
    return request({
      url: `${LAND_ALLOCATION_URLS.DELETE_LAND_ALLOCATION}/${landAllocationId}`,
      method: 'delete',
    })
  },
  updateLandAllocation(data) {
    return request({
      url: LAND_ALLOCATION_URLS.UPDATE_LAND_CONTRACTOR_INFO,
      method: 'put',
      data,
    })
  },
  getLandAllocations(pageNum, pageSize) {
    return request({
      url: `${LAND_ALLOCATION_URLS.GET_LAND_ALLOCATIONS}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  searchLandAllocations(keyword, pageNum, pageSize) {
    return request({
      url: `${LAND_ALLOCATION_URLS.SEARCH_LAND_ALLOCATIONS}/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },

  // 农作物相关
  addCrop(data) {
    return request({ url: CROP_URLS.ADD_CROP, method: 'post', data })
  },
  deleteCrop(id) {
    return request({ url: `${CROP_URLS.DELETE_CROP}/${id}`, method: 'delete' })
  },
  updateCrop(data) {
    return request({ url: CROP_URLS.UPDATE_CROP, method: 'put', data })
  },
  getCropsByPage(pageNum, pageSize) {
    return request({ url: `${CROP_URLS.GET_CROPS_BY_PAGE}/${pageNum}/${pageSize}`, method: 'get' })
  },
  searchCropsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `${CROP_URLS.SEARCH_CROPS_BY_PAGE}/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },

  //计划相关
  addPlantingPlan(data) {
    return request({ url: '/plantingPlan/add', method: 'post', data })
  },
  getPlantingPlansByPage(pageNum, pageSize) {
    return request({ url: `/plantingPlan/getPlansByPage/${pageNum}/${pageSize}`, method: 'get' })
  },
  deletePlantingPlan(id) {
    return request({ url: `/plantingPlan/delete/${id}`, method: 'delete' })
  },
  updatePlantingPlan(data) {
    return request({ url: '/plantingPlan/update', method: 'put', data })
  },
  searchPlantingPlansByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/plantingPlan/searchPlansByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  updatePlantingPlanStatus(planId, newStatus) {
    return request({
      url: `/plantingPlan/updateStatus/${planId}/${newStatus}`,
      method: 'put',
    })
  },
  getPlantingPlanById(planId) {
    return request({
      url: `/plantingPlan/getPlantingPlanById/${planId}`,
      method: 'get',
    })
  },
  getPublishedPlans() {
    return request({
      url: '/plantingPlan/getPublishedPlantingPlans',
      method: 'get',
    })
  },
  getMyPublishedPlan() {
    return request({
      url: '/plantingPlan/getPublishedPlantingPlanByUserId',
      method: 'get',
    })
  },
  getMyPlans() {
    return request({
      url: '/plantingPlan/getMyPlans',
      method: 'get',
    })
  },
  getCreatorNameById(planId) {
    return request({
      url: `/plantingPlan/getUserNameByPlanId/${planId}`,
      method: 'get',
    })
  },

  //设备相关
  addEquipment(data) {
    return request({ url: '/equipment/addEquipment', method: 'post', data })
  },
  getEquipmentByPage(pageNum, pageSize) {
    return request({ url: `/equipment/getEquipmentByPage/${pageNum}/${pageSize}`, method: 'get' })
  },
  deleteEquipment(id) {
    return request({ url: `/equipment/deleteEquipment/${id}`, method: 'delete' })
  },
  updateEquipment(data) {
    return request({ url: '/equipment/updateEquipment', method: 'put', data })
  },
  searchEquipmentByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/equipment/searchEquipmentByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getEquipmentNameAndTypeNameById(applyId) {
    return request({
      url: `/equipmentApply/getEquipmentNameAndTypeNameById/${applyId}`,
      method: 'get',
    })
  },
  getMyEquipmentByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/equipmentRecord/getByUserId/${pageNum}/${pageSize}`,
      method: 'get',
      params: {
        keyword,
      },
    })
  },
  getMyEquipmentList() {
    return request({
      url: '/equipmentRecord/getMyEquipment',
      method: 'get',
    })
  },

  //设备类型相关
  addEquipmentType(equipmentTypeName) {
    return request({
      url: `/equipmentType/add?equipmentTypeName=${encodeURIComponent(equipmentTypeName)}`,
      method: 'post',
    })
  },
  getEquipmentTypes() {
    return request({ url: '/equipmentType/getEquipmentTypes', method: 'get' })
  },
  deleteEquipmentType(id) {
    return request({ url: `/equipmentType/delete/${id}`, method: 'delete' })
  },
  updateEquipmentType(data) {
    return request({ url: '/equipmentType/update', method: 'put', data })
  },

  //计划审批相关
  addPlanAdjust(data) {
    return request({ url: '/plantingPlanAdjust/addByAdmin', method: 'post', data })
  },
  getPlanAdjustsByPage(pageNum, pageSize) {
    return request({
      url: `/plantingPlanAdjust/getPlantingPlanAdjustsByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  deletePlanAdjust(id) {
    return request({ url: `/plantingPlanAdjust/delete/${id}`, method: 'delete' })
  },
  updatePlanAdjust(data) {
    return request({ url: '/plantingPlanAdjust/update', method: 'put', data })
  },
  updatePlanAdjustStatus(id, status) {
    return request({ url: `/plantingPlanAdjust/update/${id}/${status}`, method: 'put' })
  },
  searchPlanAdjustsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/plantingPlanAdjust/searchPlantingPlanAdjustsByPage/${pageNum}/${pageSize}`,
      method: 'get',
      params: {
        keyword,
      },
    })
  },
  getPlantingPlanAdjustById(adjustId) {
    return request({
      url: `/plantingPlanAdjust/getPlantingPlanAdjustsByAdjust/${adjustId}`,
      method: 'get',
    })
  },
  getMyPlanAdjusts(keyword, pageNum, pageSize) {
    return request({
      url: `/plantingPlanAdjust/getPlantingPlanAdjustsByUserIdPage/${pageNum}/${pageSize}`,
      method: 'get',
      params: {
        keyword,
      },
    })
  },
  cancelPlanAdjust(adjustId) {
    return request({
      url: `/plantingPlanAdjust/cancel`,
      method: 'get',
      params: {
        adjustId,
      },
    })
  },

  //农资相关
  addMaterial(data) {
    return request({ url: '/material/add', method: 'post', data })
  },
  deleteMaterial(id) {
    return request({ url: `/material/delete/${id}`, method: 'delete' })
  },
  updateMaterial(data) {
    return request({ url: '/material/update', method: 'put', data })
  },
  searchMaterialsByPage(typeId, keyword, pageNum, pageSize) {
    return request({
      url: `/material/searchMaterialsByPage/${pageNum}/${pageSize}`,
      params: {
        typeId,
        keyword,
      },
      method: 'get',
    })
  },
  getMaterialList() {
    return request({ url: '/material/getAll', method: 'get' })
  },
  getMaterialTypeById(materialId) {
    return request({ url: `/material/getMaterialTypeById/${materialId}`, method: 'get' })
  },

  //农资类型相关
  getMaterialTypes() {
    return request({ url: '/materialType/getAll', method: 'get' })
  },
  addMaterialType(typeName) {
    return request({
      url: '/materialType/add',
      params: {
        typeName,
      },
      method: 'post',
    })
  },
  deleteMaterialType(id) {
    return request({ url: `/materialType/delete/${id}`, method: 'delete' })
  },
  updateMaterialType(data) {
    return request({ url: '/materialType/update', method: 'put', data })
  },

  //农资申请相关
  addMaterialApproval(data) {
    return request({ url: '/materialApply/add', method: 'post', data })
  },
  getMaterialApplyByPage(pageNum, pageSize) {
    return request({
      url: `/materialApply/getMaterialApplyByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  searchMaterialApplyByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/materialApply/searchMaterialApplyByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getMyMaterialApplies(keyword, pageNum, pageSize) {
    return request({
      url: `/materialApply/getMyApplies/${pageNum}/${pageSize}`,
      method: 'get',
      params: { keyword },
    })
  },
  updateMaterialApplyStatus(applyId, status) {
    return request({ url: `/materialApply/update/${applyId}/${status}`, method: 'put' })
  },
  getMaterialApplyById(applyId) {
    return request({ url: `/materialApply/getMaterialApplyById/${applyId}`, method: 'get' })
  },
  updateMaterialApply(data) {
    return request({ url: '/materialApply/update', method: 'put', data })
  },
  deleteMaterialApply(applyId) {
    return request({ url: `/materialApply/delete/${applyId}`, method: 'delete' })
  },
  cancelMaterialApply(applyId) {
    return request({ url: `/materialApply/cancel/${applyId}`, method: 'put' })
  },
  // 农资出入库记录相关
  getMaterialStockRecordPage(keyword, pageNum, pageSize) {
    return request({
      url: `/materialStockRecord/getByPage/${pageNum}/${pageSize}`,
      method: 'get',
      params: { keyword },
    })
  },
  addMaterialStockRecord(data) {
    return request({ url: '/materialStockRecord/add', method: 'post', data })
  },
  updateMaterialStockRecord(data) {
    return request({ url: '/materialStockRecord/update', method: 'put', data })
  },
  deleteMaterialStockRecord(stockRecordId) {
    return request({ url: `/materialStockRecord/delete/${stockRecordId}`, method: 'delete' })
  },
  // 设备审批相关
  getEquipmentList() {
    return request({ url: '/equipment/getAll', method: 'get' })
  },
  getEquipmentApplyByPage(pageNum, pageSize) {
    return request({
      url: `/equipmentApply/getApplyByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  searchEquipmentApplyByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/equipmentApply/searchApplyByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getMyEquipmentApplies(keyword, pageNum, pageSize) {
    return request({
      url: `/equipmentApply/getMyApplies/${pageNum}/${pageSize}`,
      method: 'get',
      params: { keyword },
    })
  },
  addEquipmentApply(data) {
    return request({ url: '/equipmentApply/add', method: 'post', data })
  },
  updateEquipmentApply(data) {
    return request({ url: '/equipmentApply/update', method: 'put', data })
  },
  deleteEquipmentApply(applyId) {
    return request({ url: `/equipmentApply/delete/${applyId}`, method: 'delete' })
  },
  cancelEquipmentApply(applyId) {
    return request({ url: `/equipmentApply/cancel/${applyId}`, method: 'put' })
  },
  updateEquipmentApplyStatus(applyId, status) {
    return request({ url: `/equipmentApply/update/${applyId}/${status}`, method: 'put' })
  },
  getEquipmentTypeNameById(equipmentId) {
    return request({
      url: `/equipment/getEquipmentTypeNameById/${equipmentId}`,
      method: 'get',
    })
  },

  //种植记录相关
  addPlantingRecord(data) {
    return request({ url: '/plantingRecord/add', method: 'post', data })
  },
  getPlantingRecordByPage(pageNum, pageSize) {
    return request({
      url: `/plantingRecord/getPlantingRecordsByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getPlantingRecordByUserIdPage(pageNum, pageSize) {
    return request({
      url: `/plantingRecord/getMyPlantingRecords/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  updatePlantingRecord(data) {
    return request({ url: '/plantingRecord/update', method: 'put', data })
  },
  deletePlantingRecord(recordId) {
    return request({ url: `/plantingRecord/delete/${recordId}`, method: 'delete' })
  },
  getPlantingRecordById(recordId) {
    return request({ url: `/plantingRecord/getPlantingRecordById/${recordId}`, method: 'get' })
  },
  getLandAll() {
    return request({ url: '/land/getAll', method: 'get' })
  },
  getLandByUserId() {
    return request({ url: '/landAllocation/getMyLands', method: 'get' })
  },
  getPlantingPlanAll() {
    return request({ url: '/plantingPlan/getAll', method: 'get' })
  },
  getCropAll() {
    return request({ url: '/crop/getAll', method: 'get' })
  },
  getPlantingPlanByLandId(landId) {
    return request({ url: `/plantingPlan/getByLandId/${landId}`, method: 'get' })
  },
  getGrowthPlantingRecordByPage(pageNum, pageSize) {
    return request({
      url: `/plantingRecord/getGrowthPlantingRecordsByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getMyPlantingRecords(pageNum, pageSize) {
    return request({
      url: `/plantingRecord/getMyPlantingRecords/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },

  //成熟作物相关
  getMatureCropByPage(pageNum, pageSize) {
    return request({
      url: `/matureCrop/getMatureCropsByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  addMatureCrop(data) {
    return request({ url: '/matureCrop/add', method: 'post', data })
  },
  updateMatureCrop(data) {
    return request({ url: '/matureCrop/update', method: 'put', data })
  },
  deleteMatureCrop(matureCropId) {
    return request({ url: `/matureCrop/delete/${matureCropId}`, method: 'delete' })
  },
  getOutputQuantity(recordId) {
    return request({ url: `/matureCrop/getOutputQuantity/${recordId}`, method: 'get' })
  },
  getMatureCropStatistics(params) {
    return request({
      url: '/matureCrop/statistics',
      method: 'get',
      params,
    })
  },

  // 农事活动记录相关 (FarmOperationRecordController)
  getLandActivitiesByRecordId(recordId, page, size) {
    return request({
      url: `/farmOperation/getOperationPageByRecordId/${recordId}/${page}/${size}`,
      method: 'get',
    })
  },
  addLandActivity(data) {
    return request({ url: '/farmOperation/add', method: 'post', data })
  },
  deleteLandActivity(operationId) {
    return request({ url: `/farmOperation/delete/${operationId}`, method: 'delete' })
  },
  updateLandActivity(data) {
    return request({ url: '/farmOperation/update', method: 'put', data })
  },

  // 承包人农资库存相关 (ContractorMaterialStockController)
  getContractorMaterialsByPage(pageNum, pageSize) {
    return request({
      url: `/contractorMaterialStock/getLandAllocationByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  getMyContractorMaterialsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/contractorMaterialStock/getByUserId/${pageNum}/${pageSize}`,
      method: 'get',
      params: {
        keyword,
      },
    })
  },
  searchContractorMaterialsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/contractorMaterialStock/searchLandAllocationInfoByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  updateContractorMaterialStock(data) {
    return request({
      url: '/contractorMaterialStock/updateStock',
      method: 'put',
      data,
    })
  },
  deleteContractorMaterialStock(contractorMaterialStockId) {
    return request({
      url: `/contractorMaterialStock/delete/${contractorMaterialStockId}`,
      method: 'delete',
    })
  },

  // 设备记录相关 (EquipmentRecordController)
  getEquipmentRecordsByPage(pageNum, pageSize) {
    return request({
      url: `/equipmentRecord/getByPage/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  searchEquipmentRecordsByPage(keyword, pageNum, pageSize) {
    return request({
      url: `/equipmentRecord/searchByPage/${keyword}/${pageNum}/${pageSize}`,
      method: 'get',
    })
  },
  updateEquipmentRecordStatus(data) {
    return request({
      url: '/equipmentRecord/updateStatus',
      method: 'put',
      data,
    })
  },
  deleteEquipmentRecord(equipmentRecordId) {
    return request({
      url: `/equipmentRecord/delete/${equipmentRecordId}`,
      method: 'delete',
    })
  },

  // 农业智能助手相关
  getChatHistory() {
    return request({
      url: '/assistant/history',
      method: 'get',
    })
  },
  clearChatHistory() {
    return request({
      url: '/assistant/clear',
      method: 'delete',
    })
  },

  // 设备报修申请相关 (EquipmentRepairApplyController)
  addEquipmentRepairApply(data) {
    return request({
      url: '/equipmentRepairApply/add',
      method: 'post',
      data,
    })
  },
  getEquipmentRepairApplyByRecordId(recordId, applicantName, phone) {
    return request({
      url: '/equipmentRepairApply/getByRecordId',
      method: 'get',
      params: { recordId, applicantName, phone },
    })
  },
}

export default api
