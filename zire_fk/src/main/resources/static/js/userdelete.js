function confirmDelete(id) {
    if(confirm("Silmək istədiyinizə əminsiniz?")) {
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');

        fetch('/koch/delete/' + id, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': token,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if(response.ok) {
                const row = document.getElementById('row-' + id);
                if(row) row.remove();
            } else if (response.status === 404) {
                alert("İstifadəçi tapılmadı!");
            } else {
                alert("Silinmə zamanı xəta baş verdi!");
            }
        })
        .catch(err => {
            alert("Xəta: " + err);
        });
    }
}
