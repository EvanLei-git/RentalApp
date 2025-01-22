function selectRole(role) {
    document.querySelector('.role-selection-container').classList.add('hidden');
    document.querySelector('.register-container').classList.add('expanded');

    setTimeout(() => {
        const selectedForm = document.getElementById(`${role}Form`);
        selectedForm.style.display = 'flex';
        setTimeout(() => {
            selectedForm.classList.add('active');
        }, 50);
    }, 300);
}

function goBack() {
    document.querySelectorAll('.registration-form').forEach(form => {
        form.classList.remove('active');
    });
    document.querySelector('.register-container').classList.remove('expanded');

    setTimeout(() => {
        document.querySelector('.role-selection-container').classList.remove('hidden');
        setTimeout(() => {
            document.querySelectorAll('.registration-form').forEach(form => {
                form.style.display = 'none';
            });
        }, 500);
    }, 300);
}

async function handleRegister(event, role) {
    event.preventDefault();


    try {
        const form = event.target;
        const formData = new FormData(form);
        formData.append('role', role);

        const response = await fetch('/register', {
            method: 'POST',
            body: formData
        });

        let result;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            result = await response.json();
        } else {
            const text = await response.text();
            result = {message: text};
        }



        if (response.ok) {
            await Swal.fire({
                icon: 'success',
                title: 'Registration Successful',
                text: 'You can now login with your credentials',
                customClass: {
                    confirmButton: 'swal2-confirm-btn'
                },
                buttonsStyling: false
            });
            window.location.href = '/login';
        } else {
            throw new Error(result.message || 'Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);


        Swal.fire({
            icon: 'error',
            title: 'Registration Failed',
            text: error.message || 'An unexpected error occurred during registration',
            customClass: {
                confirmButton: 'swal2-confirm-btn'
            },
            buttonsStyling: false
        });
    }
}