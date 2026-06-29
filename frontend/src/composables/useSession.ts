import { ref } from 'vue'

const STORAGE_KEY_ID = 'lightborrow_employee_id'
const STORAGE_KEY_NAME = 'lightborrow_employee_name'

const employeeId = ref<string>('')
const employeeName = ref<string>('')

export function useSession() {
  if (!employeeId.value) {
    loadSession()
  }

  function setEmployee(id: string, name: string) {
    employeeId.value = id
    employeeName.value = name
    localStorage.setItem(STORAGE_KEY_ID, id)
    localStorage.setItem(STORAGE_KEY_NAME, name)
  }

  function clearEmployee() {
    employeeId.value = ''
    employeeName.value = ''
    localStorage.removeItem(STORAGE_KEY_ID)
    localStorage.removeItem(STORAGE_KEY_NAME)
  }

  return { employeeId, employeeName, setEmployee, clearEmployee }
}

function loadSession() {
  employeeId.value = localStorage.getItem(STORAGE_KEY_ID) ?? ''
  employeeName.value = localStorage.getItem(STORAGE_KEY_NAME) ?? ''
}
