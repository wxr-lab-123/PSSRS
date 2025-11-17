import axios from 'axios'

/**
 * 阿里云OSS上传（通过后端接口）
 * @param {File} file - 文件对象
 * @param {string} folder - 存储文件夹（可选，如 'doctors', 'patients'）
 * @returns {Promise<{url: string, path: string}>}
 */
export function uploadToOSS(file, folder = 'doctors') {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)
    if (folder) {
      formData.append('folder', folder)
    }

    const token = localStorage.getItem('auth_token')
    const headers = {
      'Content-Type': 'multipart/form-data'
    }
    if (token) {
      headers.Authorization = token
    }

    axios.post('/api/admin/files/upload', formData, { headers })
      .then(response => {
        const res = response.data
        if (res.code === 0) {
          resolve(res.data) // {url: 'https://...', path: '/uploads/...'}
        } else {
          // 业务错误：保留业务信息
          reject({ code: res.code, msg: res.msg || '上传失败', data: res.data })
        }
      })
      .catch(error => {
        const status = error?.response?.status
        reject({
          httpStatus: status,
          msg: error?.response?.data?.msg || error.message || '上传失败',
          raw: error
        })
      })
  })
}

/**
 * 上传医生头像
 * @param {File} file - 图片文件
 * @returns {Promise<string>} 返回图片URL
 */
export function uploadDoctorAvatar(file) {
  return uploadToOSS(file, 'doctors').then(data => data.url || data.path)
}

