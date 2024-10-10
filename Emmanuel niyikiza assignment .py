from collections import deque

class TelemedicineScheduler:
    def __init__(self):
        self.available_doctors = ["Dr. emmanuel", "Dr. niyikiza", "Dr. tuyisenge"]
        self.appointments = []
        self.undo_stack = []
        self.request_queue = deque()

    def book_appointment(self, doctor_name):
        if doctor_name in self.available_doctors:
            appointment = f"Appointment booked with {doctor_name}"
            self.appointments.append(appointment)
            self.undo_stack.append(appointment)  # Push to undo stack
            print(appointment)
        else:
            print(f"{doctor_name} is not available.")

    def undo_booking(self):
        if self.undo_stack:
            last_appointment = self.undo_stack.pop()
            self.appointments.remove(last_appointment)
            print(f"Cancelled: {last_appointment}")
        else:
            print("No appointments to undo.")

    def request_appointment(self, doctor_name):
        if doctor_name in self.available_doctors:
            self.request_queue.append(doctor_name)
            print(f"Requested appointment with {doctor_name}")
        else:
            print(f"{doctor_name} is not available.")

    def process_request(self):
        if self.request_queue:
            doctor_name = self.request_queue.popleft()
            self.book_appointment(doctor_name)
        else:
            print("No appointment requests to process.")

    def show_appointments(self):
        print("Current Appointments:")
        for appointment in self.appointments:
            print(appointment)

    def show_available_doctors(self):
        print("Available Doctors:")
        for doctor in self.available_doctors:
            print(doctor)

# Example usage
if __name__ == "__main__":
    scheduler = TelemedicineScheduler()
    scheduler.show_available_doctors()
    
    scheduler.request_appointment("Dr. emmanuel")
    scheduler.process_request()
    scheduler.show_appointments()
    
    scheduler.request_appointment("Dr. niyikiza")
    scheduler.process_request()
    scheduler.show_appointments()
    
    scheduler.undo_booking()
    scheduler.show_appointments()
