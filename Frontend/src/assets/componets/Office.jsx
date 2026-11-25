import axios from 'axios';
import { useEffect, useState } from 'react';

function Office() {
    const [offices, setOffices] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [searchText, setSearchText] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [deletingOfficeId, setDeletingOfficeId] = useState(null);
    const [officeName, setOfficeName] = useState('');
    const [officeNumber, setOfficeNumber] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        async function loadOffices() {
            try {
                const response = await axios.get("http://localhost:8084/api/office/all");
                if (!Array.isArray(response.data)) {
                    throw new Error("The offices response was not a list.");
                }
                setOffices(response.data);
            } catch (error) {
                const responseMessage = error.response?.data;
                setErrorMessage(
                    typeof responseMessage === 'string'
                        ? responseMessage
                        : error.message || "Unable to load offices. Check that the clinic server is running."
                );
            } finally {
                setIsLoading(false);
            }
        }

        loadOffices();
    }, []);

    async function saveOffice(event) {
        event.preventDefault();
        setErrorMessage('');
        setIsSaving(true);

        try {
            await axios.post("http://localhost:8084/api/office/save", {
                name: officeName.trim(),
                officeNumber: Number(officeNumber)
            });
            const response = await axios.get("http://localhost:8084/api/office/all");
            setOffices(response.data);
            setOfficeName('');
            setOfficeNumber('');
        } catch (error) {
            const responseMessage = error.response?.data;
            setErrorMessage(
                typeof responseMessage === 'string'
                    ? responseMessage
                    : error.message || "Unable to save the office."
            );
        } finally {
            setIsSaving(false);
        }
    }

    async function deleteOffice(office) {
        setErrorMessage('');
        setDeletingOfficeId(office.id);

        try {
            await axios.delete(`http://localhost:8084/api/office/delete/${office.id}`);
            setOffices(currentOffices => currentOffices.filter(item => item.id !== office.id));
        } catch (error) {
            const responseMessage = error.response?.data;
            if (
                typeof responseMessage === 'string' &&
                responseMessage.toLowerCase().includes('assigned to a doctor')
            ) {
                setErrorMessage(
                    `Cannot delete office ${office.officeNumber} because it is assigned to a doctor. Reassign the doctor to another office first.`
                );
            } else {
                setErrorMessage(
                    typeof responseMessage === 'string'
                        ? responseMessage
                        : error.message || `Unable to delete office ${office.officeNumber}.`
                );
            }
        } finally {
            setDeletingOfficeId(null);
        }
    }

    const normalizedSearch = searchText.trim().toLowerCase();
    const filteredOffices = offices.filter(office =>
        String(office.name || '').toLowerCase().includes(normalizedSearch) ||
        String(office.officeNumber).includes(normalizedSearch)
    );
    
  return (
    <div>
      <h2>Save Office</h2>
      <form onSubmit={saveOffice}>
        <label htmlFor="office-name">Office Name: </label>
        <input
          id="office-name"
          type="text"
          value={officeName}
          onChange={event => setOfficeName(event.target.value)}
          required
        />
        <br/><br/>
        <label htmlFor="office-number">Office Number: </label>
        <input
          id="office-number"
          type="number"
          value={officeNumber}
          onChange={event => setOfficeNumber(event.target.value)}
          required
        />
        <br/><br/>
        <button type="submit" disabled={isSaving}>
          {isSaving ? 'Saving...' : 'Save'}
        </button>
      </form>

      <h1>Available Offices</h1>
      <label htmlFor="office-search">Search by office name or number: </label>
      <input
        id="office-search"
        type="search"
        value={searchText}
        onChange={event => setSearchText(event.target.value)}
      /><br/><br/>  
      {errorMessage && <p role="alert">{errorMessage}</p>}

      <table border="1" style={{ margin: '0 auto' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Office Number</th>
            <th>Office Name</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredOffices.map(office => (
            <tr key={office.id}>
              <td>{office.id}</td>
              <td>{office.officeNumber}</td>
              <td>{office.name}</td>
              <td>
                <button
                  type="button"
                  onClick={() => deleteOffice(office)}
                  disabled={deletingOfficeId === office.id}
                >
                  {deletingOfficeId === office.id ? 'Deleting...' : 'Delete'}
                </button>
              </td>
            </tr>
          ))}
          {!isLoading && filteredOffices.length === 0 && (
            <tr>
              <td colSpan="4">No offices found.</td>
            </tr>
          )}
          {isLoading && (
            <tr>
              <td colSpan="4">Loading offices...</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

export default Office
