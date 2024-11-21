import { useState } from "react";

export default function Form() {
  const [originalUrl, setOriginalUrl] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [urlCode, setUrlCode] = useState("");
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();

    const date = new Date(expirationDate);
    const expirationTime = Math.floor(date.getTime() / 1000) + "";

    try {
      const response = await fetch("https://ymln273cg2.execute-api.us-east-2.amazonaws.com/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ originalUrl, expirationTime }),
      });
      const data = await response.json();
      setUrlCode(data.code);
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <div className="form-container">
      <form onSubmit={handleSubmit}>
        <div className="form-input">
          <label>Original URL:</label>
          <input type="text" value={originalUrl} placeholder="https://example.com" onChange={(event) => setOriginalUrl(event.target.value)} />
        </div>
        <div className="form-input">
          <label>Expiration Date:</label>
          <input type="date" value={expirationDate} onChange={(event) => setExpirationDate(event.target.value)} />
        </div>
        <button type="submit">Short it!</button>
      </form>
      {urlCode && <p className="url-code">Get your URL Code: <span>{urlCode}</span></p>}
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
}
