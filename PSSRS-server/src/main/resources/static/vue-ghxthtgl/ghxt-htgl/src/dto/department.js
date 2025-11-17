/**
 * 科室 DTO (Data Transfer Object) - 用于前后端数据传输
 */

/**
 * 创建科室 DTO
 * @typedef {Object} CreateDepartmentDTO
 * @property {string} name - 科室名称
 * @property {string} [description] - 科室简介
 */

/**
 * 更新科室 DTO
 * @typedef {Object} UpdateDepartmentDTO
 * @property {string} name - 科室名称
 * @property {string} [description] - 科室简介
 */

/**
 * 转换 UI 表单数据为创建科室 DTO
 * @param {Object} formData - UI 表单数据
 * @returns {CreateDepartmentDTO}
 */
export function toCreateDepartmentDTO(formData) {
  return {
    name: formData.name?.trim() || '',
    description: formData.description?.trim() || ''
  }
}

/**
 * 转换 UI 表单数据为更新科室 DTO
 * @param {Object} formData - UI 表单数据
 * @returns {UpdateDepartmentDTO}
 */
export function toUpdateDepartmentDTO(formData) {
  return {
    name: formData.name?.trim() || '',
    description: formData.description?.trim() || ''
  }
}

