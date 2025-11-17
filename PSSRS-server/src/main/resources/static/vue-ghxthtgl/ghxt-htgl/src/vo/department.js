/**
 * 科室 VO (View Object) - 用于前端展示
 */

/**
 * 科室视图对象
 * @typedef {Object} DepartmentVO
 * @property {number} id - 科室ID
 * @property {string} name - 科室名称
 * @property {string} [description] - 科室简介
 * @property {string} [createTime] - 创建时间
 * @property {string} [updateTime] - 更新时间
 */

/**
 * 科室树形结构 VO
 * @typedef {Object} DepartmentTreeVO
 * @property {number} id - 科室ID
 * @property {string} name - 科室名称
 * @property {string} [description] - 科室简介
 * @property {DepartmentTreeVO[]} [children] - 子科室列表
 */

/**
 * 从后端数据转换为科室 VO
 * @param {Object} data - 后端返回的数据
 * @returns {DepartmentVO}
 */
export function fromDepartmentVO(data) {
  if (!data) return null
  return {
    id: data.id,
    name: data.name || '',
    description: data.description || '',
    createTime: data.createTime || data.create_time,
    updateTime: data.updateTime || data.update_time
  }
}

/**
 * 从后端数据转换为科室列表 VO
 * @param {Array} list - 后端返回的列表数据
 * @returns {DepartmentVO[]}
 */
export function fromDepartmentListVO(list) {
  if (!Array.isArray(list)) return []
  return list.map(item => fromDepartmentVO(item))
}

/**
 * 从后端数据转换为科室树形结构 VO
 * @param {Array} tree - 后端返回的树形数据
 * @returns {DepartmentTreeVO[]}
 */
export function fromDepartmentTreeVO(tree) {
  if (!Array.isArray(tree)) return []
  return tree.map(node => ({
    id: node.id,
    name: node.name || '',
    description: node.description || '',
    children: node.children ? fromDepartmentTreeVO(node.children) : []
  }))
}

